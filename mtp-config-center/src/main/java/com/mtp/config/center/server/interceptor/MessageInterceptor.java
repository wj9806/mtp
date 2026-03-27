package com.mtp.config.center.server.interceptor;

import com.mtp.core.netty.MessageRequest;
import io.netty.channel.ChannelHandlerContext;

public interface MessageInterceptor {

    boolean preHandle(MessageRequest request, ChannelHandlerContext ctx);

}
