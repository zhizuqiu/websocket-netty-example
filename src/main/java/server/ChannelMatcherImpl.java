package server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;

public class ChannelMatcherImpl implements ChannelMatcher {
    private String channelMatcher;

    public ChannelMatcherImpl(String channelMatcher) {
        this.channelMatcher = channelMatcher;
    }

    @Override
    public boolean matches(Channel channel) {
        return this.channelMatcher.equals(ChannelMaps.get(channel.id().asLongText()));
    }
}
