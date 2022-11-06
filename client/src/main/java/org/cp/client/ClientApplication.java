package org.cp.client;

import java.net.URI;
import org.cp.client.client.WebsocketClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {

	public static void main(String[] args) {
		new SpringApplication(ClientApplication.class).run(args);
	}

	@Override
	public void run(String... args) throws Exception {

		this.connect();
	}

	private void connect() {
		URI uri = URI.create("ws://localhost:8080/chat");
		WebsocketClient client = new WebsocketClient(uri);
		client.connect();
	}
}
