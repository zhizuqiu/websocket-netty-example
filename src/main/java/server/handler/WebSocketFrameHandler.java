/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
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
                ChannelGroups.broadcast(new TextWebSocketFrame(request), channelMatcher);
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
