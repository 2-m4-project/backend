package com.stenden.inf2j.alarmering.server.sql.migrator;

public class Migration {

    private final String id;
    private final String sql;

    private Migration(String id, String sql) {
        this.id = id;
        this.sql = sql;
    }

    public String getId() {
        return id;
    }

    public String getSql() {
        return sql;
    }

    public static Migration create(String id, String sql){
        return new Migration(id, sql);
    }
}
