package com.stenden.inf2j.alarmering.server.http.handler.session;

import com.stenden.inf2j.alarmering.api.auth.AuthenticationResult;
import com.stenden.inf2j.alarmering.api.auth.User;
import com.stenden.inf2j.alarmering.api.auth.UserService;
import com.stenden.inf2j.alarmering.api.session.SessionStorage;
import com.stenden.inf2j.alarmering.server.http.request.CreateSessionRequest;
import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.api.RequestHandler;
import nl.jk5.jsonlibrary.JsonObject;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class CreateSessionHandler implements RequestHandler<CreateSessionRequest, JsonObject> {

    @Inject
    private UserService userService;

    @Inject
    private SessionStorage sessionStorage;

    @Override
    public CompletableFuture<JsonObject> handle(RequestContext ctx, CreateSessionRequest request) throws Exception {
        return this.userService.authenticate(request.username(), request.password()).thenCompose(res -> {
            if(res instanceof AuthenticationResult.Success){
                AuthenticationResult.Success<User> result = (AuthenticationResult.Success<User>) res;
                
                return sessionStorage.getSession(ctx)
                        .thenCompose(session -> sessionStorage.authenticateSession(session, result.getUser()))
                        .thenApply(session -> new JsonObject().set("success",  true).set("user", session.getUser().toJson()));
            }
            if(((AuthenticationResult) res) instanceof AuthenticationResult.IncorrectCredentials){
                return CompletableFuture.completedFuture(new JsonObject().set("success",  true).set("status", "incorrect-credentials"));
            }
            if(((AuthenticationResult) res) instanceof AuthenticationResult.InsufficientPermissions){
                return CompletableFuture.completedFuture(new JsonObject().set("success",  true).set("status", "insufficient-perms"));
            }
            if(((AuthenticationResult) res) instanceof AuthenticationResult.TwoFactorAuthRequired){
                return CompletableFuture.completedFuture(new JsonObject().set("success",  true).set("status", "2fa-required"));
            }
            if(((AuthenticationResult) res) instanceof AuthenticationResult.UserDoesNotExist){
                return CompletableFuture.completedFuture(new JsonObject().set("success",  true).set("status", "incorrect-credentials"));
            }
            return CompletableFuture.completedFuture(new JsonObject().set("success",  true).set("status", "incorrect-credentials"));
        });
    }
}
