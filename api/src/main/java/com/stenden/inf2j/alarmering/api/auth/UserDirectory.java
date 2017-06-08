package com.stenden.inf2j.alarmering.api.auth;

import nl.jk5.jsonlibrary.JsonObject;

import java.util.concurrent.CompletableFuture;

public interface UserDirectory {

    /**
     * The type of the directory. This gives a back-reference to the main type
     * @return The type of this directory
     */
    UserDirectoryType getType();

    /**
     * The name of this directory.
     * This name is user-defined upon creation of this directory. Should be unique across directories
     * @return The name of this directory.
     */
    String getName();

    JsonObject getSettings();

    CompletableFuture<DirectoryUser> createUser(String username, String firstname, String lastname, String displayname, String email);

    CompletableFuture<AuthenticationResult<DirectoryUser>> authenticate(String username, String password);
}
