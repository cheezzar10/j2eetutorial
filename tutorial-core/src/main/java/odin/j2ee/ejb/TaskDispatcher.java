package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.model.TaskExecution;

@MessageDriven(name = "TaskDispatcher", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/tasks"),
	})
// TODO rename to TaskExecutor
public class TaskDispatcher implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onMessage(Message message) {
		ObjectMessage execMsg = (ObjectMessage)message;
		try {
			TaskExecution execution = execMsg.getBody(TaskExecution.class);
			log.debug("performing task {} execution request with parameters: {}", execution.getTaskName(), execution.getTaskParams());
			// TODO find task definition and start it using TaskRegistry singleton
		} catch (JMSException jmsEx) {
			throw new EJBException("task activation failed", jmsEx);
		}
	}
}
