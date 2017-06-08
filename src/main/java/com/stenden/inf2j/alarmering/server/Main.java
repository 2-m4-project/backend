package com.stenden.inf2j.alarmering.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.stenden.inf2j.alarmering.api.util.annotation.NonnullByDefault;
import com.stenden.inf2j.alarmering.server.inject.GuiceModule;

@NonnullByDefault
public final class Main {

    private Main(){
        throw new UnsupportedOperationException("No instances");
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());

        AlarmeringServer server = injector.getInstance(AlarmeringServer.class);
        server.start();
    }
}
