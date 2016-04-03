package odin.j2ee.api;

import odin.j2ee.model.TaskExecution;

public interface TaskManager {
	void execute(TaskExecution task);
}
