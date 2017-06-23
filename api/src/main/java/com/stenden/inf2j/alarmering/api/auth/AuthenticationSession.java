package com.stenden.inf2j.alarmering.api.auth;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * An authentication session
 */
public interface AuthenticationSession {

    /**
     * @return The moment the session started
     */
    Instant sessionStart();

    /**
     * @return The future of the user that this session belongs to
     */
    CompletableFuture<User> user();
}
