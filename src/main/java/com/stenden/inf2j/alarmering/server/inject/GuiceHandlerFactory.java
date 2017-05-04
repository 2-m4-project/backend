package com.stenden.inf2j.alarmering.server.inject;

import com.google.inject.Injector;
import nl.jk5.http2server.api.HandlerFactory;
import nl.jk5.http2server.api.RequestHandler;

import javax.inject.Inject;

public class GuiceHandlerFactory implements HandlerFactory {

    private final Injector injector;

    @Inject
    public GuiceHandlerFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    public RequestHandler create(Class<? extends RequestHandler> cls) {
        return this.injector.getInstance(cls);
    }
}
