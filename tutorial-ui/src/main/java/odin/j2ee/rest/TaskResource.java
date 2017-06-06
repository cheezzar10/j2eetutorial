package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.TaskManager;
import odin.j2ee.model.TaskExecution;

@Path("/tasks")
public class TaskResource {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	public TaskResource() {
		log.debug("new TaskResource instance created  @{}", this);
	}
	
	@EJB
	private TaskManager taskMgr;
	
	@POST
	@Consumes("application/json")
	public void execute(TaskExecution execution) {
		log.debug("received execution request for task: {} with parameters: {}", execution.getTaskName(), execution.getTaskParams());
		log.debug("sending task execution request");
		taskMgr.execute(execution);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> getcCacheStats() {
		return taskMgr.getCacheStats();
	}
}
