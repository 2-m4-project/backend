package com.stenden.inf2j.alarmering.api.util;

import nl.jk5.jsonlibrary.JsonElement;

/**
 * This interface represents an object that can be converted to json
 */
public interface JsonConvertible {

    /**
     * Convert this object to json
     * @return The json representation of this object
     */
    JsonElement toJson();
}
