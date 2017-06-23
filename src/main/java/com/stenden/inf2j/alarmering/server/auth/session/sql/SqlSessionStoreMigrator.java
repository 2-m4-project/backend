package com.stenden.inf2j.alarmering.server.auth.session.sql;

import com.stenden.inf2j.alarmering.server.sql.migrator.Migration;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migrator;

import javax.inject.Inject;

public class SqlSessionStoreMigrator {

    @Inject
    public SqlSessionStoreMigrator(Migrator migrator) {
        migrator.addMigration(Migration.create("create sessions table v1", "CREATE TABLE sessions (session_key CHAR(64) PRIMARY KEY NOT NULL, user_id INT NOT NULL, authenticated BOOL DEFAULT FALSE NOT NULL, last_seen TIMESTAMP DEFAULT NOW() NOT NULL, csrf_token CHAR(32) NOT NULL, ip VARCHAR(46) NOT NULL, CONSTRAINT table_name_user_id_fk FOREIGN KEY (user_id) REFERENCES user (id));"));
    }
}
