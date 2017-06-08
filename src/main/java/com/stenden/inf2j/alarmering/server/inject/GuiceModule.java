package com.stenden.inf2j.alarmering.server.inject;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.stenden.inf2j.alarmering.api.history.HistoryService;
import com.stenden.inf2j.alarmering.api.sql.SqlProvider;
import com.stenden.inf2j.alarmering.api.util.annotation.NonnullByDefault;
import com.stenden.inf2j.alarmering.server.history.SqlHistoryService;
import com.typesafe.config.Config;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

@NonnullByDefault
public class GuiceModule implements Module {

    private static final Logger logger = LoggerFactory.getLogger(GuiceModule.class);

    @Override
    public void configure(Binder binder) {
        EventLoopGroup masterGroup;
        EventLoopGroup childGroup;
        boolean usingEpoll;
        if(Epoll.isAvailable()){
            logger.debug("Epoll transport is available. Using native epoll");
            masterGroup = new EpollEventLoopGroup();
            childGroup = new EpollEventLoopGroup();
            usingEpoll = true;
        }else{
            logger.debug("Epoll transport is not available. Using java NIO");
            masterGroup = new NioEventLoopGroup();
            childGroup = new NioEventLoopGroup();
            usingEpoll = false;
        }

        binder.bind(Executor.class).toProvider(ExecutorProvider.class);
        binder.bind(EventExecutor.class).toProvider(EventExecutorProvider.class);
        binder.bind(EventLoopGroup.class).annotatedWith(Names.named("Master")).toInstance(masterGroup);
        binder.bind(EventLoopGroup.class).annotatedWith(Names.named("Child")).toInstance(childGroup);
        binder.bind(boolean.class).annotatedWith(Names.named("UsingEpoll")).toInstance(usingEpoll);
        binder.bind(Config.class).toProvider(ConfigProvider.class);
        binder.bind(SqlProvider.class).to(HikariSqlProvider.class);
        binder.bind(HistoryService.class).to(SqlHistoryService.class);
    }
}
