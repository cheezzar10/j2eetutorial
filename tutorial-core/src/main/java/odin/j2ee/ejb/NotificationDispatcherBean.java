package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;
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

public class NotificationDispatcherBean implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onMessage(Message message) {
		try {
			Queue queue = (Queue) message.getJMSDestination();
			log.debug("notification message received from queue: {}", queue.getQueueName());
			TextMessage notification = (TextMessage) message;
			String subscriptionId = notification.getStringProperty("subscriptionId");
			registry.dispatchNotification(subscriptionId, notification.getText());
		} catch (JMSException | DispatchingFailedException dispatchingEx) {
			log.error("notification dispatching failed: ", dispatchingEx);
		}
	}
}
