package com.stenden.inf2j.alarmering.api.history;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HistoryService {

    CompletableFuture<List<HistoryElement>> getHistoryForClient(int clientId);
}
