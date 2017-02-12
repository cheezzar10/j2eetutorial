package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DispatchingFailedException;
import odin.j2ee.api.NotificationSubscriptionRegistry;

@MessageDriven(name = "NotificationDispatcher", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:jms/queue/notifications"),
	})
public class NotificationDispatcherBean implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Resource
	private MessageDrivenContext context;
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onMessage(Message message) {
		try {
			String subscriptionId = message.getStringProperty("subscriptionId");
			Queue queue = (Queue) message.getJMSDestination();
			log.debug("dispatching message received from queue: {} using dispatcher @{} to subscription {}", queue.getQueueName(), hashCode(), subscriptionId);
			
			TextMessage notification = (TextMessage) message;
			registry.dispatchNotification(subscriptionId, notification.getText());
			
			// TODO detect close channel message with JMSXGroupSeq = -1
		} catch (JMSException | DispatchingFailedException dispatchingEx) {
			log.error("notification dispatching failed: ", dispatchingEx);
		}
	}
}
