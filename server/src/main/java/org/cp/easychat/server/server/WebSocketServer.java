package org.cp.easychat.server.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketServer {

	private NioEventLoopGroup boss;
	private NioEventLoopGroup workers;
	private ServerBootstrap serverBootstrap;

	public void start() {
		this.init();
		try {
			ChannelFuture future = serverBootstrap.bind(8888);
			log.info("server start success.");
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void init() {
		// 配置启动器
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		serverBootstrap.option(ChannelOption.TCP_NODELAY, true);
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

		boss = new NioEventLoopGroup();
		workers = new NioEventLoopGroup(7);

		serverBootstrap.group(boss, workers)
				.channel(NioServerSocketChannel.class)
				.childHandler(getChildHandlers());


	}

	private ChannelHandler getChildHandlers() {
		return new ChannelInitializer<>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new HttpServerCodec());// http协议的编解码器
				pipeline.addLast(new ChunkedWriteHandler());// 大数据流支持， 切成小块传输
				pipeline.addLast(new HttpObjectAggregator(64*1024));// 聚合器，对应上面的切块
				pipeline.addLast(new WebSocketServerProtocolHandler("/chat"));// 握手 心跳处理
				pipeline.addLast(new MyWebSocketHandler());// 我的业务处理Handler
			}
		};
	}

}
