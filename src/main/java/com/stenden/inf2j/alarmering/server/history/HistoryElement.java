package com.stenden.inf2j.alarmering.server.history;

import com.google.common.base.MoreObjects;
import com.stenden.inf2j.alarmering.server.util.JsonConvertible;
import nl.jk5.jsonlibrary.JsonElement;
import nl.jk5.jsonlibrary.JsonObject;

import java.time.Instant;

public class HistoryElement implements JsonConvertible {

    private final int clientId;
    private final float lat;
    private final float lng;
    private final String melding;
    private final Instant time;

    public HistoryElement(int clientId, float lat, float lng, String melding, Instant time) {
        this.clientId = clientId;
        this.lat = lat;
        this.lng = lng;
        this.melding = melding;
        this.time = time;
    }

    public int getClientId() {
        return clientId;
    }

    public float getLat() {
        return lat;
    }

    public float getLng() {
        return lng;
    }

    public String getMelding() {
        return melding;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("clientId", clientId)
                .add("lat", lat)
                .add("lng", lng)
                .add("melding", melding)
                .add("time", time)
                .toString();
    }

    @Override
    public JsonElement toJson() {
        return new JsonObject()
                .add("client-id", this.clientId)
                .add("lat", this.lat)
                .add("long", this.lng)
                .add("melding", this.melding)
                .add("time", this.time.toEpochMilli());
    }
}
