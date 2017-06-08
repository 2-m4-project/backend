package com.stenden.inf2j.alarmering.api.auth;

import com.stenden.inf2j.alarmering.api.util.JsonConvertible;
import nl.jk5.jsonlibrary.JsonElement;
import nl.jk5.jsonlibrary.JsonObject;

public interface User extends JsonConvertible {

    int id();

    String username();

    String firstName();
    
    String lastName();

    String displayName();

    String email();

    @Override
    default JsonElement toJson() {
        return new JsonObject()
                .add("id", this.id())
                .add("username", this.username())
                .add("display-name", this.displayName());
    }
}
