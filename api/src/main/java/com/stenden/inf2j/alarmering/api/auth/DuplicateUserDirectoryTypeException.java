package com.stenden.inf2j.alarmering.api.auth;

/**
 * This exception will be thrown if a directory type is registered that already exists
 */
public final class DuplicateUserDirectoryTypeException extends RuntimeException {

    public DuplicateUserDirectoryTypeException(String type) {
        super("User directory type with name " + type + " already exists");
    }
}
