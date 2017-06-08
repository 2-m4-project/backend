package com.stenden.inf2j.alarmering.server.http.handler.session;

import com.stenden.inf2j.alarmering.api.auth.UserService;
import com.stenden.inf2j.alarmering.api.session.SessionStorage;
import nl.jk5.http2server.api.Request;
import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.api.RequestHandler;
import nl.jk5.jsonlibrary.JsonNull;
import nl.jk5.jsonlibrary.JsonObject;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class WhoAmIHandler implements RequestHandler<Request, JsonObject> {

    @Inject
    private UserService userService;

    @Inject
    private SessionStorage sessionStorage;

    @Override
    public CompletableFuture<JsonObject> handle(RequestContext ctx, Request request) throws Exception {
        return this.sessionStorage.getSession(ctx).thenApply((session) -> {
            return new JsonObject()
                    .add("is-authenticated", session.isAuthenticated())
                    .add("user", session.getUser() == null ? JsonNull.INSTANCE : session.getUser().toJson());
        });
    }
}
