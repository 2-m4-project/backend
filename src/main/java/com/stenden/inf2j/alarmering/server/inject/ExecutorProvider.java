package com.stenden.inf2j.alarmering.server.inject;

import io.netty.channel.EventLoopGroup;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.concurrent.Executor;

public class ExecutorProvider implements Provider<Executor> {

    @Inject
    @Named("Master")
    private EventLoopGroup eventLoop;

    @Override
    public Executor get() {
        return eventLoop.next();
    }
}
