package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DnsRecordManager;
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
	
	@Context
	private Providers providers;
	
	@Resource
	private UserTransaction tx;
	
	@POST
	@Consumes("application/json")
	public void execute(TaskExecution execution) {
		log.debug("received execution request for task: {} with parameters: {}", execution.getTaskName(), execution.getTaskParams());
		
		Class<TaskExecution> taskExecClass = TaskExecution.class;
		MessageBodyWriter<TaskExecution> msgBodyWrtr = providers.getMessageBodyWriter(taskExecClass, null, null, MediaType.APPLICATION_JSON_TYPE);
		log.debug("message body writer: {}", msgBodyWrtr);
		
		log.debug("sending task execution request");
		taskMgr.execute(execution);
	}
	
	/*
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> getCacheStats() {
		try {
			tx.begin();
			try {
				DnsRecordManager dnsRecMgr = (DnsRecordManager) new InitialContext().lookup("java:global/tutorial-app/tutorial-core/DnsRecordManager");
				dnsRecMgr.removeRecord(1);
				Thread.sleep(10);
				dnsRecMgr.removeRecord(1);
				Map<String, String> result = taskMgr.getCacheStats();
				tx.commit();
				return result;
			} catch (Exception ex) {
				tx.rollback();
				throw ex;
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		}
	}
	 */
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> getCacheStats() {
		try {
			DnsRecordManager dnsRecMgr = (DnsRecordManager) new InitialContext().lookup("java:global/tutorial-app/tutorial-core/DnsRecordManager");
			// dnsRecMgr.removeRecord(1);
			Thread.sleep(10);
			return taskMgr.getCacheStats();
		} catch (Exception ex) {
			throw new EJBException(ex);
		}
	}
}
