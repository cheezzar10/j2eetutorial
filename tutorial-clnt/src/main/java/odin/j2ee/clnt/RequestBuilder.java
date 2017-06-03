package odin.j2ee.clnt;

import java.util.Map;

public class RequestBuilder {
	public static String buildExecuteTaskRequest(String taskName, Map<String, String> taskParams) {
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
