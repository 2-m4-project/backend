package com.stenden.inf2j.alarmering.api.auth;

import nl.jk5.jsonlibrary.JsonObject;

public interface UserDirectoryType {

    String name();

    String description();

    default UserDirectory createDirectory(String name){
        return this.createDirectory(name, null);
    }

    UserDirectory createDirectory(String name, JsonObject settings);
}
