package com.stenden.inf2j.alarmering.server.auth.directory.sql;

import com.stenden.inf2j.alarmering.api.auth.DirectoryUser;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class SqlDirectoryUser implements DirectoryUser {

    private final int id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String displayName;
    private final String email;

    SqlDirectoryUser(ResultSet rs) throws SQLException {
        this.id = rs.getInt("id");
        this.username = rs.getString("username");
        this.firstName = rs.getString("firstname");
        this.lastName = rs.getString("lastname");
        this.displayName = rs.getString("displayname");
        this.email = rs.getString("email");
    }

    public SqlDirectoryUser(int id, String username, String firstName, String lastName, String displayName, String email) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.email = email;
    }

    @Override
    public String uuid() {
        return String.valueOf(this.id);
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
}
