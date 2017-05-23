package com.stenden.inf2j.alarmering.server;

import com.google.inject.Injector;
import com.stenden.inf2j.alarmering.server.http.AlertHandler;
import com.stenden.inf2j.alarmering.server.http.DemoHandler;
import com.stenden.inf2j.alarmering.server.http.HistoryHandler;
import com.stenden.inf2j.alarmering.server.http.HomeHandler;
import com.stenden.inf2j.alarmering.server.inject.GuiceHandlerFactory;
import com.stenden.inf2j.alarmering.server.util.annotation.NonnullByDefault;
import com.typesafe.config.Config;
import io.netty.channel.EventLoopGroup;
import nl.jk5.http2server.routing.HttpServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

@NonnullByDefault
public final class AlarmeringServer {

    private static final Logger logger = LoggerFactory.getLogger(AlarmeringServer.class);

    private final Config config;
    private final Injector injector;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup childGroup;
    private final boolean useEpoll;

    @Inject
    public AlarmeringServer(Injector injector, Config config, @Named("Master") EventLoopGroup masterGroup, @Named("Child") EventLoopGroup childGroup, @Named("UsingEpoll") boolean useEpoll) {
        this.config = config;
        this.injector = injector;
        this.masterGroup = masterGroup;
        this.childGroup = childGroup;
        this.useEpoll = useEpoll;
    }

    public void start(){
        logger.info("Starting alarmering server");

        HttpServerBuilder.create()
                .eventLoop(this.masterGroup, this.childGroup)
                .useEpoll(this.useEpoll)
                .handlerFactory(this.injector.getInstance(GuiceHandlerFactory.class))
                .router()
                    .GET(1000, "/api/geschiedenis/:id", HistoryHandler.class)
                    .GET(1000, "/api/locatie/:id", AlertHandler.class)
                    .GET(1000, "/api/nieuws/", HomeHandler.class)
                .end()
                .start(this.config.getConfig("http-server"));

        // Login en Register moeten nog
    }
}
