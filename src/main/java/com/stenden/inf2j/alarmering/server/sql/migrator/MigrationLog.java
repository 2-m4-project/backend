package com.stenden.inf2j.alarmering.server.sql.migrator;

import java.time.Instant;

public class MigrationLog {

    private final int id;
    private final String migrationId;
    private final String sql;
    private final Instant timestamp;

    private boolean success;
    private String error;

    public MigrationLog(int id, String migrationId, String sql, boolean success, String error, Instant timestamp) {
        this.id = id;
        this.migrationId = migrationId;
        this.sql = sql;
        this.success = success;
        this.error = error;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getMigrationId() {
        return migrationId;
    }

    public String getSql() {
        return sql;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setError(String error) {
        this.error = error;
    }
}
