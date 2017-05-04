package com.stenden.inf2j.alarmering.server.http;

import nl.jk5.http2server.api.annotation.Path;

import javax.annotation.Nullable;
import java.util.Optional;

public interface DemoRequest {

    @Path
    String naam();

    @Path
    @Nullable
    String naam2();

    @Path
    Optional<String> naam3();
}
