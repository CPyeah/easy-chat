package org.cp.easychat.server.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		log.info("⬆ new connection from {}", ctx.channel().remoteAddress());
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		log.info("️ connection close from {}", ctx.channel().remoteAddress());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.info(" New Message: {}, from {}", msg, ctx.channel().remoteAddress());
	}
}
