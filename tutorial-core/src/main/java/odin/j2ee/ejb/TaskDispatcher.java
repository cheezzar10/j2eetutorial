package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.TaskRegistry;
import odin.j2ee.model.TaskExecution;

@MessageDriven(name = "TaskDispatcher", activationConfig = {
		// @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "java:jboss/DefaultJMSConnectionFactory2"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/tasks"),
		@ActivationConfigProperty(propertyName = "maxSession", propertyValue = "8"),
		// @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "teskName = 'Install Package'")
	})
// TODO rename to TaskExecutor
public class TaskDispatcher implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final AtomicBoolean slowElected = new AtomicBoolean();
	
	private boolean slow;
	
	@EJB
	private TaskRegistry taskRegistry;
	
	@Resource
	private MessageDrivenContext ctx;
	
	@PostConstruct
	private void init() {
		if (!slowElected.get()) {
			if (slowElected.compareAndSet(false, true)) {
				slow = true;
				
				log.debug("task dispatcher @{} elected as throttled", hashCode());
			}
		}
	}
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onMessage(Message message) {
		log.debug("task dispatcher @{}", hashCode());
		
		ObjectMessage execMsg = (ObjectMessage)message;
		try {
			log.debug("task execution request message #{} received delivery count: {}", 
					message.getJMSMessageID(), message.getObjectProperty("JMSXDeliveryCount"));
			
			TaskExecution execution = execMsg.getBody(TaskExecution.class);
			log.debug("performing task {} execution request with parameters: {}", execution.getTaskName(), execution.getTaskParams());
			
			if (slow) {
				try {
					log.debug("throttling task execution");

					Thread.sleep(10_000);
				} catch (InterruptedException intrEx) {
					Thread.currentThread().interrupt();
					return;
				}
			}

			// TODO find task definition and start it using TasskRegistry singleton
			boolean success = taskRegistry.executeTask(execution.getTaskName(), execution.getTaskParams());
			if (!success) {
				log.debug("task execution failed - rolling back");
				ctx.setRollbackOnly();
			}
		} catch (JMSException jmsEx) {
			throw new EJBException("task activation failed", jmsEx);
		}
	}
}
