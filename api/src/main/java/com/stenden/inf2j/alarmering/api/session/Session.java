package com.stenden.inf2j.alarmering.api.session;

import com.stenden.inf2j.alarmering.api.auth.AuthenticationSession;
import com.stenden.inf2j.alarmering.api.auth.User;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Session {

    CompletableFuture<AuthenticationSession> requireAuthentication();

    CompletableFuture<Optional<AuthenticationSession>> getAuthenticationSession();

    String getSessionKey();

    boolean isAuthenticated();

    Instant getLastSeen();

    User getUser();

    String getCsrfToken();

    String getIp();
}
