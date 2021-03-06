package com.stenden.inf2j.alarmering.server.http.handler;

import com.stenden.inf2j.alarmering.api.history.HistoryService;
import com.stenden.inf2j.alarmering.api.util.JsonConvertible;
import com.stenden.inf2j.alarmering.api.util.annotation.NonnullByDefault;
import com.stenden.inf2j.alarmering.server.http.request.HistoryRequest;
import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.api.RequestHandler;
import nl.jk5.jsonlibrary.JsonCollectors;
import nl.jk5.jsonlibrary.JsonObject;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

@NonnullByDefault
public class HistoryHandler implements RequestHandler<HistoryRequest, JsonObject> {

    @Inject
    private HistoryService historyService;

    @Override
    public CompletableFuture<JsonObject> handle(RequestContext ctx, HistoryRequest request) throws Exception {
        return this.historyService.getHistoryForClient(request.id())
                .thenApply((history) -> history.stream().map(JsonConvertible::toJson).collect(JsonCollectors.toJsonArray()))
                .thenApply((history) -> new JsonObject().add("response", history));
    }
}
