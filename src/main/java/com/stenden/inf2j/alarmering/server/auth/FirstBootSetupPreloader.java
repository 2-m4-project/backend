package com.stenden.inf2j.alarmering.server.auth;

import com.stenden.inf2j.alarmering.server.sql.migrator.Migration;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migrator;

import javax.inject.Inject;

public class FirstBootSetupPreloader {

    @Inject
    public FirstBootSetupPreloader(Migrator migrator){
        migrator.addMigration(Migration.create("create SQL directory", "INSERT INTO user_directories (type, name, priority) VALUES ('SQL', 'sql-directory', 1000)"));
        migrator.addMigration(Migration.create("create admin user in SQL directory", "INSERT INTO user_directory (username, firstname, lastname, displayname, email, password_hash, directory_id) VALUES ('admin', 'Administrator', '', 'Administrator', '', '$s0$e0801$BVciMTW5PoQhgC+c+8gDTA==$79k2+6CLUPDyHK70OyiBdfLF+w6oNcoPHucPNkSP4sQ=', 1)"));
        migrator.addMigration(Migration.create("reference admin user to SQL  directory", "INSERT INTO `user` (username, firstname, lastname, displayname, email, directory_id, directory_uuid) VALUES ('admin', 'Administrator', '', 'Administrator', '', 1, '1')"));
    }
}
