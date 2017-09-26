package server.tools;

import io.netty.channel.Channel;
import server.bean.ChannelGroups;
import server.bean.ChannelMaps;

import java.util.ArrayList;
import java.util.List;

public class ChannelOrganize {
    private ChannelOrganize() {
    }

    public static void clean() {
        if (ChannelGroups.size() != ChannelMaps.size()) {
            List<Channel> list = new ArrayList<>();
            for (Channel key : ChannelMaps.keySet()) {
                if (!ChannelGroups.contains(key)) {
                    list.add(key);
                }
            }
            for (Channel channel : list) {
                ChannelMaps.remove(channel);
            }
        }
    }
}
