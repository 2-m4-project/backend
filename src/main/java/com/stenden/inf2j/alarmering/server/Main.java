package com.stenden.inf2j.alarmering.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.stenden.inf2j.alarmering.api.util.annotation.NonnullByDefault;
import com.stenden.inf2j.alarmering.server.inject.GuiceModule;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migration;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migrator;

@NonnullByDefault
public final class Main {

    private Main(){
        throw new UnsupportedOperationException("No instances");
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());

        Migrator migrator = injector.getInstance(Migrator.class);
        migrator.addMigration(Migration.create("create migration_log table", "create table migration_log (id int auto_increment primary key, migration_id varchar(255) not null, `sql` text not null, error text null, timestamp timestamp default CURRENT_TIMESTAMP not null, success BOOL NOT NULL);"));

        AlarmeringServer server = injector.getInstance(AlarmeringServer.class);
        server.start();
    }
}
