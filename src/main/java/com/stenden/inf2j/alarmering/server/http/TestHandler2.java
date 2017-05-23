package com.stenden.inf2j.alarmering.server.http;

import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.api.RequestHandler;
import nl.jk5.jsonlibrary.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class TestHandler2 implements RequestHandler<IdRequest, JsonObject> {

    private static final Logger logger = LoggerFactory.getLogger(TestHandler2.class);

    @Override
    public CompletableFuture<JsonObject> handle(RequestContext ctx, IdRequest request) throws Exception {
        logger.info("Melding");
        return null;
    }
}
