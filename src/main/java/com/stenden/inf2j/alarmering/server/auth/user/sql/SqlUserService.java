package com.stenden.inf2j.alarmering.server.auth.user.sql;

import com.google.common.collect.ImmutableSortedSet;
import com.stenden.inf2j.alarmering.api.auth.*;
import com.stenden.inf2j.alarmering.api.sql.SqlProvider;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migration;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migrator;
import nl.jk5.jsonlibrary.JsonElement;
import nl.jk5.jsonlibrary.JsonObject;
import nl.jk5.jsonlibrary.JsonParser;
import nl.jk5.jsonlibrary.JsonParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SqlUserService implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(SqlUserService.class);

    private final List<UserDirectoryType> directoryTypes = new ArrayList<>();

    private final Map<String, UserDirectoryType> directoryTypeMap = new HashMap<>();

    private final SqlProvider sqlProvider;
    private final Executor executor;

    private SortedSet<UserDirectoryContainer> directoryContainers = new TreeSet<>();

    @Inject
    public SqlUserService(SqlProvider sqlProvider, Executor executor, Migrator migrator) {
        this.sqlProvider = sqlProvider;
        this.executor = executor;

        migrator.addMigration(Migration.create("create user_directories table v1", "CREATE TABLE user_directories(id SERIAL NOT NULL, type VARCHAR NOT NULL, name VARCHAR NOT NULL, priority INT NOT NULL, settings TEXT);"));
        migrator.addMigration(Migration.create("create user_directories table id primary key", "ALTER TABLE user_directories ADD CONSTRAINT user_directories_id_pk PRIMARY KEY (id);"));
        migrator.addMigration(Migration.create("create user storage table v1", "CREATE TABLE \"user\"(id SERIAL NOT NULL CONSTRAINT user_id_pk PRIMARY KEY,username VARCHAR(255) NOT NULL,firstname VARCHAR(255) NOT NULL,lastname VARCHAR(255) NOT NULL,displayname VARCHAR(255) NOT NULL,email VARCHAR(255) NOT NULL,directory_id INT NOT NULL);"));
        migrator.addMigration(Migration.create("add user storage to directory foreign key", "ALTER TABLE \"user\" ADD CONSTRAINT user_directories_id_fk FOREIGN KEY (directory_id) REFERENCES user_directories (id);"));
        migrator.addMigration(Migration.create("add directory_uuid field to users table", "ALTER TABLE \"user\" ADD directory_uuid VARCHAR NULL;"));
    }

    @Override
    public void registerType(UserDirectoryType directoryType) throws DuplicateUserDirectoryTypeException {
        if(this.directoryTypeMap.containsKey(directoryType.name())){
            throw new DuplicateUserDirectoryTypeException(directoryType.name());
        }

        logger.debug("User directory type " + directoryType.name() + " is registered");
        this.directoryTypes.add(directoryType);
        this.directoryTypeMap.put(directoryType.name(), directoryType);
    }

    @Override
    public List<UserDirectoryType> getUserDirectoryTypes() {
        return Collections.unmodifiableList(this.directoryTypes);
    }

    @Override
    public Optional<UserDirectoryType> getDirectoryTypeByName(String name){
        return Optional.ofNullable(this.directoryTypeMap.get(name));
    }

    @Override
    public CompletableFuture<SortedSet<UserDirectoryContainer>> getUserDirectories() {
        CompletableFuture<SortedSet<UserDirectoryContainer>> promise = new CompletableFuture<>();
        this.executor.execute(() -> {
            try(Connection conn = this.sqlProvider.getConnection()){
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM user_directories");

                ImmutableSortedSet.Builder<UserDirectoryContainer> setBuilder = ImmutableSortedSet.naturalOrder();
                while(rs.next()){
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    int priority = rs.getInt("priority");
                    String settingsText = rs.getString("settings");
                    
                    Optional<UserDirectoryType> directoryTypeOpt = this.getDirectoryTypeByName(type);
                    if(!directoryTypeOpt.isPresent()){
                        logger.warn("Unknown directory type " + type + ". Skipping it");
                        continue;
                    }

                    JsonObject settings = new JsonObject();
                    if(settingsText != null){
                        try {
                            JsonElement settingsElement = JsonParser.safeParse(settingsText);
                            if(!(settingsElement instanceof JsonObject)){
                                logger.warn("Settings for directory " + name + " are not a json object. Skipping it");
                                continue;
                            }
                            settings = settingsElement.asObject();
                        } catch (JsonParserException e) {
                            logger.warn("Error while parsing settings for directory " + name + ": " + e.getMessage());
                            continue;
                        }
                    }

                    settings.set("id", id);

                    UserDirectoryType directoryType = directoryTypeOpt.get();
                    UserDirectory directory = directoryType.createDirectory(name, settings);
                    setBuilder.add(new UserDirectoryContainer(id, priority, directory));
                }
                
                rs.close();
                stmt.close();

                promise.complete(setBuilder.build());
            }catch(SQLException e){
                promise.completeExceptionally(e);
            }
        });
        return promise;
    }

    @Override
    public CompletableFuture<Void> addUserDirectory(int priority, UserDirectory directory){
        CompletableFuture<Void> promise = new CompletableFuture<>();
        this.executor.execute(() -> {
            try(Connection conn = this.sqlProvider.getConnection()){
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO user_directories(\"type\", \"name\", priority, settings) VALUES (?, ?, ?, ?) RETURNING id");
                stmt.setString(1, directory.getType().name());
                stmt.setString(2, directory.getName());
                stmt.setInt(3, priority);
                if(directory.getSettings() == null){
                    stmt.setNull(4, Types.VARCHAR);
                }else{
                    stmt.setString(4, directory.getSettings().toString());
                }

                ResultSet rs = stmt.executeQuery();
                rs.next();
                int id = rs.getInt("id");

                this.directoryContainers.add(new UserDirectoryContainer(id, priority, directory));

                promise.complete(null);
            }catch(SQLException e){
                promise.completeExceptionally(e);
            }
        });
        return promise;
    }

    @Override
    public CompletableFuture<AuthenticationResult<User>> authenticate(String username, String password) {
        CompletableFuture<AuthenticationResult<User>> promise = new CompletableFuture<>();
        this.executor.execute(() -> {
            try(Connection conn = this.sqlProvider.getConnection()){
                PreparedStatement stmt = conn.prepareStatement("SELECT id, directory_id FROM \"user\" WHERE username=?");
                stmt.setString(1, username);

                ResultSet rs = stmt.executeQuery();
                if(!rs.next()){
                    rs.close();
                    stmt.close();
                    promise.complete(AuthenticationResult.userDoesNotExist());
                    return;
                }

                int directoryId = rs.getInt("directory_id");
                UserDirectoryContainer container = null;
                for (UserDirectoryContainer c : this.directoryContainers) {
                    if(c.getId() == directoryId){
                        container = c;
                        break;
                    }
                }
                if(container == null){
                    promise.completeExceptionally(new Exception("User references to a directory that does not exist"));
                    return;
                }

                int userId = rs.getInt("id");

                rs.close();
                stmt.close();

                container.getDirectory().authenticate(username, password).handle((res, e) -> {
                    if(e != null){
                        promise.completeExceptionally(e);
                    }else{
                        if(res instanceof AuthenticationResult.Success){
                            DirectoryUser directoryUser = (DirectoryUser) ((AuthenticationResult.Success) res).getUser();
                            if(directoryUser == null){
                                promise.completeExceptionally(new NullPointerException("UserDirectory returned a null directory user"));
                                return null;
                            }
                            User user = new SqlUser(userId, directoryUser);
                            promise.complete(AuthenticationResult.success(user));
                        }else{
                            //noinspection unchecked
                            promise.complete(((AuthenticationResult<User>) (AuthenticationResult) res));
                        }
                    }
                    return null;
                });
            }catch(Exception e){
                promise.completeExceptionally(e);
            }
        });
        return promise;
    }

    @Override
    public CompletableFuture<User> createUser(UserDirectory directory, String username, String firstname, String lastname, String displayname, String email) {
        //Either create a user on the directory, or when that is not supported, import a user
        //Also send a invitation mail
        return null;
    }

    @Override
    public CompletableFuture<Void> refreshDirectories() {
        return this.getUserDirectories().thenAccept(dirs -> this.directoryContainers = dirs);
    }

    @Override
    public CompletableFuture<Optional<User>> getUser(int id) {
        CompletableFuture<Optional<User>> promise = new CompletableFuture<>();
        this.executor.execute(() -> {
            try(Connection conn = this.sqlProvider.getConnection()){
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE id=?");
                stmt.setInt(1, id);

                ResultSet rs = stmt.executeQuery();
                if(!rs.next()){
                    rs.close();
                    stmt.close();
                    promise.complete(Optional.empty());
                    return;
                }

                int directoryId = rs.getInt("directory_id");
                UserDirectoryContainer container = null;
                for (UserDirectoryContainer c : this.directoryContainers) {
                    if(c.getId() == directoryId){
                        container = c;
                        break;
                    }
                }
                if(container == null){
                    promise.completeExceptionally(new Exception("User references to a directory that does not exist"));
                    return;
                }

                int userId = rs.getInt("id");
                String username = rs.getString("username");
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                String displayName = rs.getString("displayname");
                String email = rs.getString("email");

                User user = new SqlUser(userId, username, firstName, lastName, displayName, email);
                promise.complete(Optional.of(user));

                rs.close();
                stmt.close();
            }catch(Exception e){
                promise.completeExceptionally(e);
            }
        });
        return promise;
    }
}
