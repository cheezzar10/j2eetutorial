package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.PeriodicTaskManager;
import odin.j2ee.model.PeriodicTaskSchedule;

@Singleton
@Path("/periodics")
public class PeriodicTaskResource {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private PeriodicTaskManager periodicTaskMsg;
	
	@POST
	@Consumes("application/json")
	public void schedule(PeriodicTaskSchedule schedule) {
		log.debug("scheduling periodic task using: {}", schedule);
		periodicTaskMsg.scheduleTask(schedule.getName(), schedule.getInterval());
	}
	
	@DELETE
	@Consumes("text/plain")
	public void cancel(String name) {
		log.debug("cancelling periodic task with name: {}", name);
		periodicTaskMsg.cancelTask(name);
	}
}
