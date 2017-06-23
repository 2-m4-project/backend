package com.stenden.inf2j.alarmering.server.util.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class Futures {

    private Futures() {
        throw new UnsupportedOperationException("No instances");
    }

    public static <T> CompletableFuture<T> failedFuture(Throwable t){
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }

    /**
     * Async supplier. Will execute the method in the specified {@link Executor}, and return the value as a future
     * @param supplier The supplier to execute
     * @param executor The executor thread to execute the supplier function in
     * @param <T> The type of result to return
     * @return The future of the result of the supplier function
     */
    public static <T> CompletableFuture<T> supplyAsync(AsyncSupplier<T> supplier, Executor executor){
        CompletableFuture<T> future = new CompletableFuture<T>();
        executor.execute(() -> {
            try{
                future.complete(supplier.get());
            }catch(Exception e){
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
