package com.stenden.inf2j.alarmering.server.http.request;

import nl.jk5.http2server.api.annotation.Path;
import nl.jk5.http2server.api.annotation.Positive;

public interface AlertRequest {
    @Path
    @Positive
    int id();
}
