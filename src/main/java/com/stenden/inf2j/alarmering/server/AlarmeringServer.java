package com.stenden.inf2j.alarmering.server;

import com.google.inject.Injector;
import com.stenden.inf2j.alarmering.api.util.annotation.NonnullByDefault;
import com.stenden.inf2j.alarmering.server.http.handler.AddHistoryHandler;
import com.stenden.inf2j.alarmering.server.http.handler.AlertHandler;
import com.stenden.inf2j.alarmering.server.http.handler.HistoryHandler;
import com.stenden.inf2j.alarmering.server.http.handler.HomeHandler;
import com.stenden.inf2j.alarmering.server.inject.GuiceHandlerFactory;
import com.stenden.inf2j.alarmering.server.response.JsonResponseConverter;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migration;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migrator;
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

        Migrator migrator = this.injector.getInstance(Migrator.class);
        migrator.addMigration(Migration.create("create migration_log table", "create table migration_log(id serial not null constraint migration_log_pkey primary key,\nmigration_id varchar(255) not null,\nsql text not null,\nsuccess boolean not null, error text,\ntimestamp timestamp not null);"));

        if(this.config.hasPath("preload")){
            for (String preloadClass : this.config.getStringList("preload")) {
                try {
                    logger.debug("Preloading class {}", preloadClass);
                    Class<?> cl = Class.forName(preloadClass);
                    this.injector.getInstance(cl);
                } catch (ClassNotFoundException e) {
                    logger.error("Class {} was not found. Not preloading it", preloadClass);
                }
            }
        }

        migrator.start();
        
        HttpServerBuilder.create()
                .eventLoop(this.masterGroup, this.childGroup)
                .useEpoll(this.useEpoll)
                .addResponseConverter(new JsonResponseConverter())
                .handlerFactory(this.injector.getInstance(GuiceHandlerFactory.class))
                .router()
                    .GET (1000, "/api/geschiedenis/:id", HistoryHandler.class)
                    .GET (1000, "/api/geschiedenis/:id/", HistoryHandler.class)
                    .POST(1000, "/api/geschiedenis/:id", AddHistoryHandler.class)
                    .POST(1000, "/api/geschiedenis/:id/", AddHistoryHandler.class)
                    .GET (1000, "/api/locatie/:id", AlertHandler.class)
                    .GET (1000, "/api/locatie/:id/", AlertHandler.class)
                    .GET (1000, "/api/nieuws", HomeHandler.class)
                    .GET (1000, "/api/nieuws/", HomeHandler.class)
                .end()
                .start(this.config.getConfig("http-server"));
    }
}
