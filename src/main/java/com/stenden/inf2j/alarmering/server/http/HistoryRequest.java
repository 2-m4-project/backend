package com.stenden.inf2j.alarmering.server.http;

import nl.jk5.http2server.api.annotation.Path;
import nl.jk5.http2server.api.annotation.Positive;

public interface HistoryRequest {

    @Path
    @Positive
    int id();
}
