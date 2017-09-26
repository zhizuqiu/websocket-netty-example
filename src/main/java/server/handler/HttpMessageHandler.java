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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.bean.ChannelGroups;
import server.bean.ChannelMatcherImpl;
import server.bean.WebSocketMessage;
import server.tools.CheckTools;
import server.tools.RequestParser;
import server.tools.WebSocketServerMessageCreater;

import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Outputs index page content.
 */
public class HttpMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    private static final Logger logger = LoggerFactory.getLogger(HttpMessageHandler.class);

    public HttpMessageHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.method() != POST) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Send the index page
        if (req.uri().startsWith("/putMessage")) {

            String jsonParam = RequestParser.getJsonParam(req);
            System.out.printf(jsonParam);

            WebSocketMessage param = new Gson().fromJson(jsonParam, WebSocketMessage.class);

            logger.info("{} received {}", ctx.channel(), jsonParam);

            String result = "";
            if (!CheckTools.checkParam(param.getGroup(), param.getMessage())) {
                result = "param error";
            } else {
                ChannelGroups.broadcast(new TextWebSocketFrame(param.getMessage()), new ChannelMatcherImpl(param.getGroup()));
                result = "success";
            }

            ByteBuf content = WebSocketServerMessageCreater.getContent(result);
            FullHttpResponse res = getOkFullHttpResponse(content);
            sendHttpResponse(ctx, req, res);

        } else {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static FullHttpResponse getOkFullHttpResponse(ByteBuf content) {
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpUtil.setContentLength(res, content.readableBytes());
        return res;
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            System.out.println("Keep-Alive:" + HttpUtil.isKeepAlive(req));
            System.out.println("res code:" + res.status().code());
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
