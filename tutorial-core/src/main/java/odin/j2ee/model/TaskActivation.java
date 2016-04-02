package odin.j2ee.model;

import java.io.Serializable;

public class TaskActivation implements Serializable {
	private static final long serialVersionUID = -4091655843488021711L;
	
	private Integer id;
	private String taskName;
	
	public TaskActivation() {
		
	}

	public TaskActivation(Integer id, String taskName) {
		this.id = id;
		this.taskName = taskName;
	}

	public Integer getId() {
		return id;
	}

	public String getTaskName() {
		return taskName;
	}
}