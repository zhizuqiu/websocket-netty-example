package server.handler;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelMatcher;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.bean.ChannelGroups;
import server.bean.ChannelMaps;
import server.bean.ChannelMatcherImpl;
import server.bean.WebSocketMessage;
import server.tools.ChannelOrganize;
import server.tools.CheckTools;

/**
 * Echoes uppercase content of text frames.
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled ，在WebSocketServerProtocolHandler中

        if (frame instanceof TextWebSocketFrame) {

            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            logger.info("{} received {}", ctx.channel(), request);

            WebSocketMessage webSocketMessage = new Gson().fromJson(request, WebSocketMessage.class);

            if (!CheckTools.checkParam(webSocketMessage.getType(), webSocketMessage.getGroup())) {
                ctx.writeAndFlush("param error");
                return;
            }

            Channel incoming = ctx.channel();
            if ("login".equals(webSocketMessage.getType())) {
                String group = webSocketMessage.getGroup();

                ChannelMaps.put(incoming, group);
                ChannelGroups.add(incoming);
                ChannelOrganize.clean();
                System.out.println("Client:" + incoming.remoteAddress() + "加入" + "current size:" + ChannelGroups.size());
                System.out.println("ChannelMaps.size=" + ChannelMaps.size());
            } else {
                String group = ChannelMaps.get(incoming);
                ChannelMatcher channelMatcher = new ChannelMatcherImpl(group);
                ChannelGroups.broadcast(new TextWebSocketFrame(webSocketMessage.getMessage()), channelMatcher);
            }
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    /*
    打开websockert时：
    handlerAdded->channelActive
    关闭websockert时：
    channelInactive->handlerRemoved
     */

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        Channel incoming = ctx.channel();
        ChannelMaps.remove(incoming);
        ChannelOrganize.clean();
        System.out.println("Client:" + ctx.channel().remoteAddress() + "离开" + "current size:" + ChannelGroups.size());
        System.out.println("ChannelMaps.size=" + ChannelMaps.size());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
        ChannelGroups.discard(ctx.channel());
        ChannelOrganize.clean();
        Channel incoming = ctx.channel();
        System.out.println("Client:" + incoming.remoteAddress() + "异常" + "current size:" + ChannelGroups.size());
    }
}
