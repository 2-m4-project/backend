package com.stenden.inf2j.alarmering.api.auth;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;

/**
 * UserService. Singleton. Has a list of UserDirectories, stores some user information in a local datastore
 * Authentication info like a password is retrieved from the directory itself.
 */
public interface UserService {

    /**
     * Register a new directory type in the service.
     *
     * @param directoryType The type to register
     * @throws DuplicateUserDirectoryTypeException When the user directory type already exists
     */
    void registerType(UserDirectoryType directoryType) throws DuplicateUserDirectoryTypeException;

    List<UserDirectoryType> getUserDirectoryTypes();

    Optional<UserDirectoryType> getDirectoryTypeByName(String name);

    CompletableFuture<SortedSet<UserDirectoryContainer>> getUserDirectories();

    CompletableFuture<Void> addUserDirectory(int priority, UserDirectory directory);

    CompletableFuture<AuthenticationResult<User>> authenticate(String username, String password);

    CompletableFuture<User> createUser(UserDirectory directory, String username, String firstname, String lastname, String displayname, String email);

    CompletableFuture<Void> refreshDirectories();

    CompletableFuture<Optional<User>> getUser(int id);
}
