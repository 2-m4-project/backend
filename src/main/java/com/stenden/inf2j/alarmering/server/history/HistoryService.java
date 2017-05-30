package com.stenden.inf2j.alarmering.server.history;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HistoryService {

    CompletableFuture<List<HistoryElement>> getHistoryForClient(int clientId);
}
