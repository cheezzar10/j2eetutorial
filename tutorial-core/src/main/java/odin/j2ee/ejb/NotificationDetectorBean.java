package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.DeliveryMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DispatchingFailedException;
import odin.j2ee.api.NotificationSubscriptionRegistry;

@MessageDriven(name = "MessagesDetector", activationConfig = {
	@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
	@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/topic/notifications"),
	@ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
})
public class NotificationDetectorBean implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Resource
	private MessageDrivenContext context;
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onMessage(Message msg) {
		try {
			String messageId = msg.getJMSMessageID();
			int deliveryMode = msg.getJMSDeliveryMode();
			boolean redelivered = msg.getJMSRedelivered();
			
			Integer userId = (Integer)msg.getObjectProperty("userId");
			log.debug("notification message #{} for user #{} received. delivery mode: {}, redelivered: {}, delivery time: {}, expiration time: {}", 
					messageId, msg.getIntProperty("userId"), deliveryMode == DeliveryMode.PERSISTENT ? "persistent" : "non persistent", 
							redelivered, msg.getJMSDeliveryTime(), msg.getJMSExpiration());
			
			TextMessage textMsg = (TextMessage)msg;
			try {
				registry.dispatchNotification(userId, textMsg.getText());
			} catch (DispatchingFailedException dispatchingEx) {
				log.error("dispatching failed: ", dispatchingEx.getMessage());
				// TODO ignore dispatching exception
				context.setRollbackOnly();
			}
		} catch (JMSException receivingEx) {
			log.error("incoming message processing failed: ", receivingEx);
		}
	}
}