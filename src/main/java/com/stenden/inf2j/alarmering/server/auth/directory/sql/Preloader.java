package com.stenden.inf2j.alarmering.server.auth.directory.sql;

import com.stenden.inf2j.alarmering.api.auth.UserService;

import javax.inject.Inject;

public class Preloader {

    static UserDirectoryTypeSql userDirectoryTypeSql;

    @Inject
    public Preloader(UserService service, UserDirectoryTypeSql userDirectoryTypeSql){
        Preloader.userDirectoryTypeSql = userDirectoryTypeSql;
        service.registerType(userDirectoryTypeSql);
    }
}
