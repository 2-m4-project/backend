package com.stenden.inf2j.alarmering.server.auth.session.sql;

import com.stenden.inf2j.alarmering.server.sql.migrator.Migration;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migrator;

import javax.inject.Inject;

public class SqlSessionStoreMigrator {

    @Inject
    public SqlSessionStoreMigrator(Migrator migrator) {
        migrator.addMigration(Migration.create("create sessions table v1", "CREATE TABLE sessions(session_key CHAR(64) NOT NULL CONSTRAINT sessions_pkey PRIMARY KEY, user_id INTEGER CONSTRAINT sessions_user_id_fk REFERENCES \"user\", authenticated BOOLEAN DEFAULT FALSE NOT NULL, last_seen TIMESTAMP DEFAULT now() NOT NULL, csrf_token CHAR(32) NOT NULL, ip VARCHAR(46) NOT NULL);"));
    }
}
