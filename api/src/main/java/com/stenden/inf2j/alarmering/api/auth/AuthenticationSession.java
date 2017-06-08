package com.stenden.inf2j.alarmering.api.auth;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface AuthenticationSession {

    Instant sessionStart();

    CompletableFuture<User> user();
}
