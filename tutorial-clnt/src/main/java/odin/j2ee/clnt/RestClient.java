package odin.j2ee.clnt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class RestClient {
	private static final int CONNECTIONS_LIMIT = 4;
	private static final int ITERATIONS_COUNT = 32;
	private static final int CLIENTS_COUNT = 4;
	
	public static void main(String[] args) throws Exception {
		ResteasyClientBuilder clientBldr = (ResteasyClientBuilder) ClientBuilder.newBuilder();
		clientBldr.connectionPoolSize(CONNECTIONS_LIMIT);
		
		int reqCount = ITERATIONS_COUNT * CLIENTS_COUNT;
		CountDownLatch done = new CountDownLatch(reqCount);
		InvocationCallback<Response> reqCompletionCallback = new TaskExecutionRequestCallback(done);
		Thread[] threads = new Thread[CLIENTS_COUNT];
		ResteasyClient[] clients = new ResteasyClient[CLIENTS_COUNT];
		for (int tid = 0;tid < CLIENTS_COUNT;++tid) {
			ResteasyClient client = clients[tid] = (ResteasyClient) clientBldr.build();
			threads[tid] = new Thread(() -> {
				for (int i = 0;i < ITERATIONS_COUNT;++i) {
					executeTask(client, reqCompletionCallback);
				}
			});
		}
		
		long start = System.currentTimeMillis();
		
		for (Thread thread : threads) {
			thread.start();
		}
		
		System.out.println("all task execution requests have been sent - waiting for responses");
		done.await();
		
		long stop = System.currentTimeMillis();
		
		System.out.printf("%d requests completed in %d ms%n", reqCount, stop - start);
		Stream.of(clients).forEach(Client::close);
		
		System.out.print("press ENTER to exit...");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		reader.readLine();
	}
	
	private static void executeTask(Client client, InvocationCallback<Response> reqCompletionCallback) {
		Map<String, String> taskParams = new HashMap<>();
		taskParams.put("pkgName", "agent");
		String payload = RequestBuilder.buildExecuteTaskRequest("Install Package", taskParams);
		client.target("http://localhost:8080/tutorial/rs/tasks")
				.request()
				.async()
				.post(Entity.json(payload), reqCompletionCallback);
	}
}
