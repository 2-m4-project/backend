package com.stenden.inf2j.alarmering.api.session;

import com.stenden.inf2j.alarmering.api.auth.User;
import nl.jk5.http2server.api.RequestContext;

import java.util.concurrent.CompletableFuture;

public interface SessionStorage {

    CompletableFuture<Session> getSession(RequestContext request);

    CompletableFuture<Session> authenticateSession(Session session, User user);

    CompletableFuture<Session> deauthenticateSession(Session session);
}
