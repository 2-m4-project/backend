package com.stenden.inf2j.alarmering.server.auth.session.sql;

import com.stenden.inf2j.alarmering.api.auth.AuthenticationSession;
import com.stenden.inf2j.alarmering.api.auth.User;
import com.stenden.inf2j.alarmering.api.session.Session;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SqlSession implements Session {

    private final String sessionKey;
    private final boolean authenticated;
    private final Instant lastSeen;
    private final User user;
    private final String csrfToken;
    private final String ip;

    SqlSession(String sessionKey, boolean authenticated, Instant lastSeen, User user, String csrfToken, String ip) {
        this.sessionKey = sessionKey;
        this.authenticated = authenticated;
        this.lastSeen = lastSeen;
        this.user = user;
        this.csrfToken = csrfToken;
        this.ip = ip;
    }

    @Override
    public CompletableFuture<AuthenticationSession> requireAuthentication() {
        return null;
    }

    @Override
    public CompletableFuture<Optional<AuthenticationSession>> getAuthenticationSession() {
        return null;
    }

    @Override
    public String getSessionKey() {
        return sessionKey;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public Instant getLastSeen() {
        return lastSeen;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getCsrfToken() {
        return csrfToken;
    }

    @Override
    public String getIp() {
        return ip;
    }
}
