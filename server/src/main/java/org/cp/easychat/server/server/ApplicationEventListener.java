package org.cp.easychat.server.server;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class ApplicationEventListener implements ApplicationListener<ApplicationStartedEvent> {

	// 监听spring容器是否启动完成。
	// 开启websocket服务
	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		WebSocketServer server = new WebSocketServer();
		server.start();
	}
}
