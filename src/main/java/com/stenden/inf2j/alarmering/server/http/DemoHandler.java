package com.stenden.inf2j.alarmering.server.http;

import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.api.RequestHandler;
import nl.jk5.jsonlibrary.JsonObject;

import java.util.concurrent.CompletableFuture;

public class DemoHandler implements RequestHandler<DemoRequest, JsonObject> {

    @Override
    public CompletableFuture<JsonObject> handle(RequestContext ctx, DemoRequest request) throws Exception {
        return CompletableFuture.completedFuture(new JsonObject()
                .add("success", true)
                .add("naam", request.naam()));
    }
}
