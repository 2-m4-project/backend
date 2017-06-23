package com.stenden.inf2j.alarmering.server.sql.migrator;

import com.google.common.collect.ImmutableList;
import com.stenden.inf2j.alarmering.api.sql.SqlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * This migrator will migrate the database and dynamically create new tables
 * Configuration can be added by adding {@link Migration} objects
 */
@Singleton
public class Migrator {

    private static final Logger logger = LoggerFactory.getLogger(Migrator.class);

    private final SqlProvider sqlProvider;
    private final List<Migration> migrations = new LinkedList<>();

    @Inject
    public Migrator(SqlProvider sqlProvider) {
        this.sqlProvider = sqlProvider;
    }

    /**
     * Register a new migration
     * @param migration The migration to register
     */
    public void addMigration(Migration migration){
        this.migrations.add(migration);
    }

    /**
     * Return a log of all migrations to see which succeeded
     * @return A list of {@link MigrationLog} objects
     */
    public List<MigrationLog> getMigrationLog(){
        try(Connection conn = this.sqlProvider.getConnection()){
            Statement existsStatement = conn.createStatement();
            ResultSet existsRs = existsStatement.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='alarmering' AND table_name='migration_log'");
            existsRs.next();
            if (existsRs.getInt(1) == 0) { //If the 'migration_log' table does not exist, return an empty list
                existsRs.close();
                existsStatement.close();
                return Collections.emptyList();
            }else {
                existsRs.close();
                existsStatement.close();
            }

            //Select the migration log table rows
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM migration_log ORDER BY id ASC");

            //Add the migration log rows to an immutable list
            ImmutableList.Builder<MigrationLog> builder = ImmutableList.builder();
            while(rs.next()){
                int id = rs.getInt("id");
                String migrationId = rs.getString("migration_id");
                String sql = rs.getString("sql");
                boolean success = rs.getInt("success") == 1;
                String error = rs.getString("error");
                Instant timestamp = rs.getTimestamp("timestamp").toInstant();

                MigrationLog log = new MigrationLog(id, migrationId, sql, success, error, timestamp);
                builder.add(log);
            }

            return builder.build();
        }catch(SQLException e){
            return Collections.emptyList();
        }
    }

    /**
     * Start the migration process
     */
    public void start(){
        logger.debug("Starting database migration");
        List<MigrationLog> migrationLog = this.getMigrationLog();

        try(Connection conn = this.sqlProvider.getConnection()){
            conn.setAutoCommit(false);
            //Loop through all migrations
            for (Migration migration : this.migrations) {
                Optional<MigrationLog> logOpt = migrationLog.stream().filter(l -> l.getMigrationId().equals(migration.getId())).findFirst();
                boolean exists = false;
                if(logOpt.isPresent()){
                    if(logOpt.get().isSuccess()){
                        logger.debug("Skipping migration {}, already executed", logOpt.get().getMigrationId());
                        continue;
                    }
                    exists = true;
                }

                MigrationLog log = new MigrationLog(-1, migration.getId(), migration.getSql(), false, null, Instant.now());

                try{
                    logger.info("Performing database migration: " + migration.getId());
                    Statement stmt = conn.createStatement();
                    stmt.execute(migration.getSql());
                    log.setSuccess(true);
                    conn.commit();
                }catch (SQLException e){
                    conn.rollback();
                    log.setSuccess(false);
                    log.setError(e.getMessage());
                    logger.error("Error in database migration step " + migration.getId(), e);
                }

                PreparedStatement stmt;

                if(exists){
                    //The migration already exists, but the result changed. Update it
                    stmt = conn.prepareStatement("UPDATE migration_log SET `sql`=?, `success`=?, `error`=?, `timestamp`=? WHERE migration_id=?");
                    stmt.setString(1, log.getSql());
                    stmt.setBoolean(2, log.isSuccess());
                    stmt.setString(3, log.getError());
                    stmt.setTimestamp(4, Timestamp.from(log.getTimestamp()));
                    stmt.setString(5, log.getMigrationId());
                }else{
                    //The migration is new, add it to the database
                    stmt = conn.prepareStatement("INSERT INTO migration_log(migration_id, `sql`, `success`, `error`, `timestamp`) VALUES (?, ?, ?, ?, ?)");
                    stmt.setString(1, log.getMigrationId());
                    stmt.setString(2, log.getSql());
                    stmt.setBoolean(3, log.isSuccess());
                    stmt.setString(4, log.getError());
                    stmt.setTimestamp(5, Timestamp.from(log.getTimestamp()));
                }

                stmt.execute();
                conn.commit();
            }
            conn.setAutoCommit(true);
        }catch (SQLException e){
            logger.error("Error in database migration script", e);
        }
    }
}
