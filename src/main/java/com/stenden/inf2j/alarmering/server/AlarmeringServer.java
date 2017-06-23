package com.stenden.inf2j.alarmering.server;

import com.google.inject.Injector;
import com.stenden.inf2j.alarmering.api.auth.UserService;
import com.stenden.inf2j.alarmering.api.util.annotation.NonnullByDefault;
import com.stenden.inf2j.alarmering.server.http.handler.AddHistoryHandler;
import com.stenden.inf2j.alarmering.server.http.handler.AlertHandler;
import com.stenden.inf2j.alarmering.server.http.handler.HistoryHandler;
import com.stenden.inf2j.alarmering.server.http.handler.HomeHandler;
import com.stenden.inf2j.alarmering.server.http.handler.session.CreateSessionHandler;
import com.stenden.inf2j.alarmering.server.http.handler.session.LogoutHandler;
import com.stenden.inf2j.alarmering.server.http.handler.session.WhoAmIHandler;
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
    private final UserService userService;

    @Inject
    public AlarmeringServer(Injector injector, Config config, @Named("Master") EventLoopGroup masterGroup, @Named("Child") EventLoopGroup childGroup, @Named("UsingEpoll") boolean useEpoll, UserService userService, Migrator migrator) {
        this.config = config;
        this.injector = injector;
        this.masterGroup = masterGroup;
        this.childGroup = childGroup;
        this.useEpoll = useEpoll;
        this.userService = userService;
    }

    public void start(){
        logger.info("Starting alarmering server");

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
        
        Migrator migrator = this.injector.getInstance(Migrator.class);
        migrator.addMigration(Migration.create("create homepage_news table", "CREATE TABLE alarmering.homepage_news(id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, image VARCHAR(512) NOT NULL, text LONGTEXT NOT NULL);"));
        migrator.start();

        try {
            this.userService.refreshDirectories().get();
        } catch (Exception ignored) {}
        
        HttpServerBuilder.create()
                .eventLoop(this.masterGroup, this.childGroup)
                .useEpoll(this.useEpoll)
                .addResponseConverter(new JsonResponseConverter())
                .handlerFactory(this.injector.getInstance(GuiceHandlerFactory.class))
                .router()
                    .GET   (1000, "/api/geschiedenis/:id", HistoryHandler.class)
                    .GET   (1000, "/api/geschiedenis/:id/", HistoryHandler.class)
                    .POST  (1000, "/api/geschiedenis/:id", AddHistoryHandler.class)
                    .POST  (1000, "/api/geschiedenis/:id/", AddHistoryHandler.class)
                    .GET   (1000, "/api/locatie/:id", AlertHandler.class)
                    .GET   (1000, "/api/locatie/:id/", AlertHandler.class)
                    .GET   (1000, "/api/nieuws", HomeHandler.class)
                    .GET   (1000, "/api/nieuws/", HomeHandler.class)
                    .POST  (1000, "/api/session", CreateSessionHandler.class)
                    .DELETE(1000, "/api/session", LogoutHandler.class)
                    .GET   (1000, "/api/session/whoami", WhoAmIHandler.class)
                .end()
                .start(this.config.getConfig("http-server"));
    }
}
