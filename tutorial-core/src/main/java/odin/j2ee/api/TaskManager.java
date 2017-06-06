package odin.j2ee.api;

import java.util.Map;

import odin.j2ee.model.TaskExecution;

public interface TaskManager {
	void execute(TaskExecution task);

	Map<String, String> getCacheStats();
}
