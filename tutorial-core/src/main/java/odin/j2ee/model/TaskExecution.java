package odin.j2ee.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// TODO rename to TaskExecution
public class TaskExecution implements Serializable {
	private static final long serialVersionUID = -4091655843488021711L;
	
	private String taskName;
	private Map<String, String> taskParams = new HashMap<>();
	
	public TaskExecution() {
		
	}
	
	public TaskExecution(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}
	
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Map<String, String> getTaskParams() {
		return taskParams;
	}

	public void setTaskParams(Map<String, String> taskParams) {
		this.taskParams = taskParams;
	}
}
