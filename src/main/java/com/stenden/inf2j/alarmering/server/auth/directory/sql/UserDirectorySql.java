package com.stenden.inf2j.alarmering.server.auth.directory.sql;

import com.lambdaworks.crypto.SCryptUtil;
import com.stenden.inf2j.alarmering.api.auth.AuthenticationResult;
import com.stenden.inf2j.alarmering.api.auth.DirectoryUser;
import com.stenden.inf2j.alarmering.api.auth.UserDirectory;
import com.stenden.inf2j.alarmering.api.auth.UserDirectoryType;
import com.stenden.inf2j.alarmering.api.sql.SqlProvider;
import com.stenden.inf2j.alarmering.server.util.future.Futures;
import nl.jk5.jsonlibrary.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class UserDirectorySql implements UserDirectory {

    private static final Logger logger = LoggerFactory.getLogger(UserDirectorySql.class);

    private final int id;
    private final String name;
    private final SqlProvider sqlProvider;
    private final Executor executor;

    UserDirectorySql(String name, SqlProvider sqlProvider, Executor executor, @Nullable JsonObject settings) {
        this.name = name;
        this.sqlProvider = sqlProvider;
        this.executor = executor;

        if(settings != null){
            this.id = settings.get("id").asInt();
        }else{
            this.id = -1;
        }
    }

    @Override
    public UserDirectoryType getType() {
        return Preloader.userDirectoryTypeSql;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public JsonObject getSettings() {
        return null;
    }

    @Override
    public CompletableFuture<DirectoryUser> createUser(String username, String firstname, String lastname, String displayname, String email) {
        //TODO: add user to database

        return null;
    }

    @Override
    public CompletableFuture<AuthenticationResult<DirectoryUser>> authenticate(String username, String password) {
        return Futures.supplyAsync(() -> this.authenticateSync(username, password), this.executor);
    }

    AuthenticationResult<DirectoryUser> authenticateSync(String username, String password) throws SQLException {
        try(Connection conn = this.sqlProvider.getConnection()){
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_directory WHERE username=? AND directory_id=?");
            stmt.setString(1, username);
            stmt.setInt(2, this.id);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next()){
                rs.close();
                stmt.close();
                return AuthenticationResult.userDoesNotExist();
            }

            username = rs.getString("username");
            String passwordHash = rs.getString("password_hash");

            if(this.passwordCheck(username, password, passwordHash)){
                return AuthenticationResult.success(new SqlDirectoryUser(rs));
            }else{
                return AuthenticationResult.incorrectCredentials();
            }
        }
    }

    private boolean passwordCheck(String username, String password, String hash){
        try{
            return SCryptUtil.check(password, hash);
        } catch(IllegalArgumentException e){
            //This should actually never be able to happen
            //Still, if this does happen, we need to reset the user's password
            //TODO: reset user password
            logger.error("Password hash for internal directory user " + username + " is invalid");
            throw new IllegalStateException("Error while authenticating password");
        }
    }
}
 