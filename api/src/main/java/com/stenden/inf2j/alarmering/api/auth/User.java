package com.stenden.inf2j.alarmering.api.auth;

import com.stenden.inf2j.alarmering.api.util.JsonConvertible;
import nl.jk5.jsonlibrary.JsonElement;
import nl.jk5.jsonlibrary.JsonObject;

public interface User extends JsonConvertible {

    /**
     * @return The id of the user represented
     */
    int id();

    /**
     * @return The username of the user
     */
    String username();

    /**
     * @return The first name of the user
     */
    String firstName();

    /**
     * @return The last name of the user
     */
    String lastName();

    /**
     * @return The display name of the user
     */
    String displayName();

    /**
     * @return The email address of the user
     */
    String email();

    @Override
    default JsonElement toJson() {
        return new JsonObject()
                .add("id", this.id())
                .add("username", this.username())
                .add("display-name", this.displayName());
    }
}
