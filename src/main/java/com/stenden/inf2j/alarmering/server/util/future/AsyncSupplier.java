package com.stenden.inf2j.alarmering.server.util.future;

@FunctionalInterface
public interface AsyncSupplier<T> {

    T get() throws Exception;
}
