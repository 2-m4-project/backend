package com.stenden.inf2j.alarmering.server.auth.directory.sql;

import com.stenden.inf2j.alarmering.api.auth.UserDirectory;
import com.stenden.inf2j.alarmering.api.auth.UserDirectoryType;
import com.stenden.inf2j.alarmering.api.sql.SqlProvider;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migration;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migrator;
import nl.jk5.jsonlibrary.JsonObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executor;

@Singleton
public class UserDirectoryTypeSql implements UserDirectoryType {

    private final SqlProvider sqlProvider;
    private final Executor executor;

    @Inject
    public UserDirectoryTypeSql(Migrator migrator, SqlProvider sqlProvider, Executor executor) {
        this.sqlProvider = sqlProvider;
        this.executor = executor;
        
        migrator.addMigration(Migration.create("create user_directory table v1", "CREATE TABLE user_directory(id INT AUTO_INCREMENT PRIMARY KEY,directory_id INT NOT NULL,username VARCHAR(255) NOT NULL,firstname VARCHAR(255) NOT NULL,lastname VARCHAR(255) NOT NULL,displayname VARCHAR(255) NOT NULL,email VARCHAR(255) NOT NULL,password_hash VARCHAR(255) NOT NULL);"));
        migrator.addMigration(Migration.create("add user_directory directory_id foreign key to directories table", "ALTER TABLE user_directory ADD CONSTRAINT user_directory_directories_id_fk FOREIGN KEY (directory_id) REFERENCES user_directories (id);"));
    }

    @Override
    public String name() {
        return "SQL";
    }

    @Override
    public String description() {
        return "Stores the user information in SQL";
    }

    @Override
    public UserDirectory createDirectory(String name, JsonObject settings) {
        return new UserDirectorySql(name, this.sqlProvider, this.executor, settings);
    }
}
