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
import server.bean.WebSocketMessage;
import server.bean.ChannelGroups;
import server.bean.ChannelMaps;
import server.bean.ChannelMatcherImpl;

/**
 * Echoes uppercase content of text frames.
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

        if (frame instanceof TextWebSocketFrame) {

            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            logger.info("{} received {}", ctx.channel(), request);

            WebSocketMessage webSocketMessage = new Gson().fromJson(request, WebSocketMessage.class);

            Channel incoming = ctx.channel();
            if ("login".equals(webSocketMessage.getType())) {
                String group = webSocketMessage.getGroup();

                ChannelMaps.put(incoming.id().asLongText(), group);
                ChannelGroups.add(incoming);
            } else {
                String group = ChannelMaps.get(incoming.id().asLongText());
                ChannelMatcher channelMatcher = new ChannelMatcherImpl(group);
                ChannelGroups.broadcast(new TextWebSocketFrame(request),channelMatcher);
            }
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    /*
    打开websockert时：
    handlerAdded->channelActive
    */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("Client:" + incoming.remoteAddress() + "加入" + "current size:" + ChannelGroups.size());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        //ChannelGroups.broadcast(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));
    }


    /*
    关闭websockert时：
    channelInactive->handlerRemoved
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client:" + ctx.channel().remoteAddress() + "离开" + "current size:" + ChannelGroups.size());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        Channel incoming = ctx.channel();
        ChannelMaps.remove(incoming.id().asLongText());

        ctx.close();
        //Channel incoming = ctx.channel();
        //ChannelGroups.broadcast(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ChannelGroups.discard(ctx.channel());
        Channel incoming = ctx.channel();
        System.out.println("Client:" + incoming.remoteAddress() + "异常" + "current size:" + ChannelGroups.size());
        // 当出现异常就关闭连接
    }
}
