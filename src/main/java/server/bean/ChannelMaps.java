package server.bean;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelMaps {
    private static final Map<Channel, String> CHANNELMAPS = new ConcurrentHashMap<>();

    public static String get(Channel key) {
        return CHANNELMAPS.get(key);
    }

    public static Set<Channel> keySet() {
        return CHANNELMAPS.keySet();
    }

    public static void put(Channel key, String value) {
        CHANNELMAPS.put(key, value);
    }

    public static void remove(Channel key) {
        CHANNELMAPS.remove(key);
    }

    public static Integer size() {
        return CHANNELMAPS.size();
    }
}
