package com.stenden.inf2j.alarmering.server.http.request;

import nl.jk5.http2server.api.annotation.BodyText;
import nl.jk5.http2server.api.annotation.NotEmpty;

public interface CreateSessionRequest {

    @NotEmpty
    @BodyText
    String username();

    @NotEmpty
    @BodyText
    String password();
}
