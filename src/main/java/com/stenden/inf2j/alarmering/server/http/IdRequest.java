package com.stenden.inf2j.alarmering.server.http;

import java.util.Optional;

public interface IdRequest {

    int id();

    Optional<String> naam();
}
