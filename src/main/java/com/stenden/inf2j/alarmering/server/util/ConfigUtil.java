package com.stenden.inf2j.alarmering.server.util;

import com.typesafe.config.Config;

import java.util.Collection;
import java.util.Map;

public final class ConfigUtil {

    private ConfigUtil() {
        throw new UnsupportedOperationException("No instances");
    }

    @SuppressWarnings("unchecked")
    public static String getChildKey(Config config, String name){
        Object unwrapped = config.root().get(name).unwrapped();
        if(unwrapped instanceof Map){
            return ((Map<String, Object>) unwrapped).keySet().iterator().next();
        }else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<String> getChildKeys(Config config, String name){
        Object unwrapped = config.root().get(name).unwrapped();
        if(unwrapped instanceof Map){
            return ((Map<String, Object>) unwrapped).keySet();
        }else {
            return null;
        }
    }
}
