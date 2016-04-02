package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.TaskManager;
import odin.j2ee.model.TaskActivation;

@Path("/tasks")
public class TaskResource {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private TaskManager taskMgr;
	
	@POST
	@Consumes("text/plain")
	public void activate(String taskName) {
		log.debug("creating new task: {}", taskName);
		TaskActivation task = new TaskActivation(taskName);
		taskMgr.activate(task);
	}
}
