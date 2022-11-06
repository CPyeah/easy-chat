package org.cp.client.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebsocketClient {

	private URI uri;
	private Bootstrap bootstrap;
	private EventLoopGroup eventLoopGroup;
	private ChannelPromise channelPromise;
	private Channel channel;

	public WebsocketClient(URI uri) {
		this.uri = uri;
		this.init();
	}

	public void connect() {
		try {
			channel = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
			channelPromise.sync();
			log.info("connect success and handshake complete.");
		} catch (InterruptedException e) {
			log.error("connect error, " + e.getMessage(), e);
		}
	}

	private void init() {
		bootstrap = new Bootstrap();
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);

		eventLoopGroup = new NioEventLoopGroup();

		bootstrap.group(eventLoopGroup)
				.channel(NioSocketChannel.class)
				.handler(getHandlers());
	}

	private ChannelHandler getHandlers() {
		return new ChannelInitializer<>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline channelPipeline = ch.pipeline();
				channelPipeline.addLast(new HttpClientCodec());
				channelPipeline.addLast(new HttpObjectAggregator(1048576));
				channelPipeline.addLast(new MyWebSocketHandler(getHandShaker(uri)));
			}

			private WebSocketClientHandshaker getHandShaker(URI uri) {
				return WebSocketClientHandshakerFactory
						.newHandshaker(uri, WebSocketVersion.V13, null, false, null);
			}
		};
	}

	public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {


		private final WebSocketClientHandshaker handShaker;

		public MyWebSocketHandler(WebSocketClientHandshaker handShaker) {
			this.handShaker = handShaker;
		}

		@Override
		public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
			channelPromise = ctx.newPromise();
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			log.info("handshake to : {}", ctx.channel().remoteAddress());
			this.handShaker.handshake(ctx.channel());
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
			log.info("receive data {} from {}", msg, ctx.channel().remoteAddress());
			if (handShaker.isHandshakeComplete()) {
				finishHandShaker(ctx, msg);
				return;
			}
			// handle business data
			if (!(msg instanceof TextWebSocketFrame)) {
				log.warn("{} is not a text message.", msg);
				return;
			}

			TextWebSocketFrame textMsg = (TextWebSocketFrame) msg;
			log.info("client receive a message: {}", textMsg.text());
		}

		private void finishHandShaker(ChannelHandlerContext ctx, Object msg) {
			try {
				handShaker.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
				channelPromise.setSuccess();
				log.info("handShake success.");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
