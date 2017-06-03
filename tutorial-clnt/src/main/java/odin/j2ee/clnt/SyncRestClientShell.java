package odin.j2ee.clnt;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;

public class SyncRestClientShell {
	public static void main(String[] args) {
		Console console = System.console();
		
		if (console == null) {
			System.err.println("failed to open console");
			System.exit(1);
		}
		
		SyncRestClient syncClient = new SyncRestClient(console, 1);
		
		while (true) {
			console.printf("> ");
			boolean shouldExit = processCommand(console, syncClient);
			
			if (shouldExit) {
				break;
			}
		}
	}

	private static boolean processCommand(Console console, SyncRestClient client) {
		String command = console.readLine().trim();
		
		if (command.isEmpty()) {
			return false;
		}
		
		switch (command) {
		case "exit":
			return true;
		case "send":
			Map<String, String> taskParams = new HashMap<>();
			taskParams.put("pkgName", "agent");
			String request = RequestBuilder.buildExecuteTaskRequest("Install Package", taskParams);
			client.sendRequest("http://localhost:8080/tutorial/rs/tasks", request);
			return false;
		default:
			console.printf("unknown command");
			return false;
		}
	}
}
