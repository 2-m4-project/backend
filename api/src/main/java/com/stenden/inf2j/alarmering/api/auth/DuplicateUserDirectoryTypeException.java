package com.stenden.inf2j.alarmering.api.auth;

public final class DuplicateUserDirectoryTypeException extends RuntimeException {

    public DuplicateUserDirectoryTypeException(String type) {
        super("User directory type with name " + type + " already exists");
    }
}
