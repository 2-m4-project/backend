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

    /**
     * @return The settings of this directory
     */
    JsonObject getSettings();

    /**
     * Create a new user
     * @param username The username for this user
     * @param firstname The first name of this user
     * @param lastname The last name of the new user
     * @param displayname The display name of the new user
     * @param email The email address of the new user
     * @return A future of the newly created user
     */
    CompletableFuture<DirectoryUser> createUser(String username, String firstname, String lastname, String displayname, String email);

    /**
     * Performs an authenticatoin attempt for a user
     * @param username The username to log in
     * @param password The password to use when logging in
     * @return The authentication result
     */
    CompletableFuture<AuthenticationResult<DirectoryUser>> authenticate(String username, String password);
}
