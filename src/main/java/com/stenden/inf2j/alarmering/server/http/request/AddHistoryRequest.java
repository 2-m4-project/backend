package com.stenden.inf2j.alarmering.server.http.request;

import nl.jk5.http2server.api.annotation.BodyText;
import nl.jk5.http2server.api.annotation.Path;
import nl.jk5.http2server.api.annotation.Positive;

public interface AddHistoryRequest {

    @Path
    @Positive
    int id();

    @BodyText
    float latPos();

    @BodyText
    float longPos();

    @BodyText
    String melding();
}
