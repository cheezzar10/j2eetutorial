package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.DeliveryMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscriptionRegistry;

@MessageDriven(name = "MessagesDetector", activationConfig = {
	@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
	@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/topic/notifications")
})
public class NotificationDetectorBean implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onMessage(Message msg) {
		try {
			String messageId = msg.getJMSMessageID();
			int deliveryMode = msg.getJMSDeliveryMode();
			boolean redelivered = msg.getJMSRedelivered();
			
			Integer userId = (Integer)msg.getObjectProperty("userId");
			log.debug("notification #{} for user #{} was send using {} delivery mode and was{}redelivered", messageId, msg.getIntProperty("userId"),
					deliveryMode == DeliveryMode.PERSISTENT ? "persistent" : "non persistent", redelivered ? " " : " not ");
			
			TextMessage textMsg = (TextMessage)msg;
			log.debug("notification text: '{}'", textMsg.getText());
			
			registry.dispatchNotification(userId, textMsg.getText());
		} catch (JMSException receivingEx) {
			log.error("incoming message processing failed: ", receivingEx);
		}
	}
}