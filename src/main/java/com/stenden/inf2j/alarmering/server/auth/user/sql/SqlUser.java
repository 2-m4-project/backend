package com.stenden.inf2j.alarmering.server.auth.user.sql;

import com.google.common.base.MoreObjects;
import com.stenden.inf2j.alarmering.api.auth.DirectoryUser;
import com.stenden.inf2j.alarmering.api.auth.User;

public final class SqlUser implements User {

    private final int id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String displayName;
    private final String email;

    SqlUser(int id, String username, String firstName, String lastName, String displayName, String email) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.email = email;
    }

    SqlUser(int id, DirectoryUser directoryUser){
        this.id = id;
        this.username = directoryUser.username();
        this.firstName = directoryUser.firstName();
        this.lastName = directoryUser.lastName();
        this.displayName = directoryUser.displayName();
        this.email = directoryUser.email();
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public String firstName() {
        return this.firstName;
    }

    @Override
    public String lastName() {
        return this.lastName;
    }

    @Override
    public String displayName() {
        return this.displayName;
    }

    @Override
    public String email() {
        return this.email;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .add("displayName", displayName)
                .add("email", email)
                .toString();
    }
}
