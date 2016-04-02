package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.TaskManager;
import odin.j2ee.model.TaskActivation;

@Stateless(name = "TaskManager")
public class TaskManagerBean implements TaskManager {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private Instance<JMSContext> jmsCtx;
	
	@Resource(mappedName = "java:/jms/queue/tasks")
	private Destination tasksQueue;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void activate(TaskActivation task) {
		log.debug("sending execution request for task: {}", task.getTaskName());
		JMSProducer sender = jmsCtx.get().createProducer();
		sender.send(tasksQueue, task);
	}
}
