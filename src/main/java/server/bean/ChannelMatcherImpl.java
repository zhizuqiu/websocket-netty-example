package server.bean;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;

public class ChannelMatcherImpl implements ChannelMatcher {
    private String group;

    public ChannelMatcherImpl(String group) {
        this.group = group;
    }

    @Override
    public boolean matches(Channel channel) {
        return this.group.equals(ChannelMaps.get(channel));
    }
}
