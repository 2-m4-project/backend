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

@Singleton
public class Migrator {

    private static final Logger logger = LoggerFactory.getLogger(Migrator.class);

    private final SqlProvider sqlProvider;
    private final List<Migration> migrations = new LinkedList<>();

    @Inject
    public Migrator(SqlProvider sqlProvider) {
        this.sqlProvider = sqlProvider;
    }

    public void addMigration(Migration migration){
        this.migrations.add(migration);
    }

    public List<MigrationLog> getMigrationLog(){
        try(Connection conn = this.sqlProvider.getConnection()){
            Statement existsStatement = conn.createStatement();
            ResultSet existsRs = existsStatement.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_schema='public' AND table_name='migration_log')");
            existsRs.next();
            if (!existsRs.getBoolean(1)) {
                existsRs.close();
                existsStatement.close();
                return Collections.emptyList();
            }else {
                existsRs.close();
                existsStatement.close();
            }
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM migration_log ORDER BY id ASC");

            ImmutableList.Builder<MigrationLog> builder = ImmutableList.builder();
            while(rs.next()){
                int id = rs.getInt("id");
                String migrationId = rs.getString("migration_id");
                String sql = rs.getString("sql");
                boolean success = rs.getBoolean("success");
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

    public void start(){
        logger.debug("Starting database migration");
        List<MigrationLog> migrationLog = this.getMigrationLog();

        try(Connection conn = this.sqlProvider.getConnection()){
            conn.setAutoCommit(false);
            for (Migration migration : this.migrations) {
                Optional<MigrationLog> logOpt = migrationLog.stream().filter(l -> l.getMigrationId().equals(migration.getId())).findFirst();
                if(logOpt.isPresent()){
                    if(logOpt.get().isSuccess()){
                        logger.debug("Skipping migration {}, already executed", logOpt.get().getMigrationId());
                        continue;
                    }
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

                PreparedStatement stmt = conn.prepareStatement("INSERT INTO reverb.public.migration_log(migration_id, sql, success, error, timestamp) VALUES (?, ?, ?, ?, ?)");
                stmt.setString(1, log.getMigrationId());
                stmt.setString(2, log.getSql());
                stmt.setBoolean(3, log.isSuccess());
                stmt.setString(4, log.getError());
                stmt.setTimestamp(5, Timestamp.from(log.getTimestamp()));

                stmt.executeUpdate();
                conn.commit();
            }
            conn.setAutoCommit(true);
        }catch (SQLException e){
            logger.error("Error in database migration script", e);
        }
    }
}
