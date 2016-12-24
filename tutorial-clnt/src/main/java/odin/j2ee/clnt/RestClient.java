package odin.j2ee.clnt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class RestClient {
	private static final int ITERATIONS_COUNT = 16;
	private static final int CLIENTS_COUNT = 4;
	
	public static void main(String[] args) throws Exception {
		ClientBuilder clientBldr = ClientBuilder.newBuilder();
		((ResteasyClientBuilder)clientBldr).connectionPoolSize(8);
		Client client = clientBldr.build();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		
		Thread[] threads = new Thread[CLIENTS_COUNT];
		for (int tid = 0;tid < CLIENTS_COUNT;++tid) {
			threads[tid] = new Thread(() -> {
				for (int i = 0;i < ITERATIONS_COUNT;++i) {
					// reader.readLine();
					executeTask(client);
				}
			});
			threads[tid].start();
		}
		
		for (Thread thread : threads) {
			thread.join();
		}
		
		System.out.print("press ENTER to exit...");
		reader.readLine();
	}
	
	private static void executeTask(Client client) {
		Map<String, String> taskParams = new HashMap<>();
		taskParams.put("pkgName", "agent");
		String payload = createExecuteTaskRequestPayload("Install Package", taskParams);
		System.out.printf("payload: %s%n", payload);
		Response response = client.target("http://localhost:8080/tutorial/rs/tasks")
				.request()
				.post(Entity.json(payload));
		int status = response.getStatus();
		System.out.printf("response status: %d%n", status);
		response.close();
	}

	private static String createExecuteTaskRequestPayload(String taskName, Map<String, String> taskParams) {
		StringBuilder payload = new StringBuilder();
		payload.append("{ \"taskName\": \"" + taskName + "\"");
		
		if (!taskParams.isEmpty()) {
			payload.append(", \"taskParams\": { ");
			boolean firstParam = true;
			for (Map.Entry<String, String> param : taskParams.entrySet()) {
				if (!firstParam) {
					payload.append(", ");
				}
				payload.append("\" " + param.getKey() + "\": \"" + param.getValue() + "\"");
				firstParam = false;
			}
			payload.append(" }");
		}
		payload.append(" }");
		
		return payload.toString();
	}
}
