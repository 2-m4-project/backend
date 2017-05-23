package com.stenden.inf2j.alarmering.server.inject;

import com.stenden.inf2j.alarmering.server.util.annotation.NonnullByDefault;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

@NonnullByDefault
public class EventExecutorProvider implements Provider<EventExecutor> {

    @Inject
    @Named("Master")
    private EventLoopGroup eventLoop;

    @Override
    public EventExecutor get() {
        return eventLoop.next();
    }
}
