package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelMaps {
    private static final Map<String, String> CHANNELMAPS = new ConcurrentHashMap<>();

    public static String get(String key) {
        return CHANNELMAPS.get(key);
    }

    public static void put(String key, String value) {
        CHANNELMAPS.put(key, value);
    }

    public static void remove(String key) {
        CHANNELMAPS.remove(key);
    }

    public static Integer size() {
        return CHANNELMAPS.size();
    }
}
