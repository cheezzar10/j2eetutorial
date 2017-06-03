package odin.j2ee.clnt;

import java.io.Console;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class SyncRestClient {
	private final ResteasyClientBuilder builder;
	private final Console console;
	private ResteasyClient client;
	
	public SyncRestClient(Console console, int connPoolSize) {
		ResteasyClientBuilder builder = (ResteasyClientBuilder) ClientBuilder.newBuilder();
		builder.connectionPoolSize(connPoolSize);
		// not necessary anymore, closed connections will be detected by NOT_FOUND status
		builder.connectionCheckoutTimeout(3, TimeUnit.SECONDS);
		
		this.builder = builder;
		this.console = console;
		
		client = builder.build();
	}
	
	public void sendRequest(String endpoint, String request) {
		if (client == null) {
			console.printf("connecting%n");
			client = builder.build();
		}
		
		Response response = client.target(endpoint)
				.request()
				.post(Entity.json(request));

		console.printf("response status: %d%n", response.getStatus());
		console.printf("response: %s%n", response.getEntity());

		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			console.printf("disconnecting%n");
			client.close();
			client = null;
		}
	}
}
