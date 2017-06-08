package com.stenden.inf2j.alarmering.server.inject.dynamic;

import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.stenden.inf2j.alarmering.api.auth.UserService;
import com.stenden.inf2j.alarmering.server.util.ConfigUtil;
import com.typesafe.config.Config;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class UserServiceProvider implements Provider<UserService> {

    private final Class<?> implClass;
    private final Injector injector;

    //Example config:
    /*
    users {
      service {
        "com.stenden.inf2j.alarmering.server.auth.user.sql.SqlUserService" {
          // Module-specific config
        }
      }
    }
    */

    @Inject
    public UserServiceProvider(Config config, Injector injector) throws ClassNotFoundException {
        Config usersConfig = config.getConfig("users");
        Config serviceConfig = usersConfig.getConfig("service");

        String clName = ConfigUtil.getChildKey(usersConfig, "service");

        this.implClass = Class.forName(clName);
        this.injector = injector.createChildInjector((binder) -> {
            binder.bind(Config.class).annotatedWith(Names.named("ServiceConfig")).toInstance(serviceConfig.getConfig("\"" + clName + "\""));
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public UserService get() {
        return this.injector.getInstance((Class<UserService>) this.implClass);
    }
}
