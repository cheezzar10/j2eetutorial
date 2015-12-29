package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.ConnectionMetaData;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscriptionRegistry;

@MessageDriven(name = "MessagesDetector", activationConfig = {
	@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
	@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/topic/notifications"),
	@ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
})
// TODO rename to NotificationDistributorBean
public class NotificationDetectorBean implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Resource
	private MessageDrivenContext context;
		
	@Inject
	private JMSContext jmsCtx;
	
	@Resource(mappedName = "java:/jms/queue/notifications")
	private Destination queue;
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@PostConstruct
	private void init() {
		ConnectionMetaData jmsConnMeta = jmsCtx.getMetaData();
		try {
			@SuppressWarnings("unchecked")
			Enumeration<String> jmsxPropNames = jmsConnMeta.getJMSXPropertyNames();
			while (jmsxPropNames.hasMoreElements()) {
				String jmsxPropName = jmsxPropNames.nextElement();
				log.debug("JMSX property name: {}", jmsxPropName);
			}
		} catch (JMSException jmsEx) {
			log.debug("failed to dump JMSX property names", jmsEx);
		}
	}
	
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
			
			JMSProducer distrubutor = jmsCtx.createProducer();
			Set<String> userChannelIds = registry.getUserChannelIds(userId);
			for (String channelId : userChannelIds) {
				log.debug("distributing notification {} to channel {}", messageId, channelId);
				TextMessage notificationMsg = jmsCtx.createTextMessage(textMsg.getText());
				notificationMsg.setStringProperty("subscriptionId", channelId);
				notificationMsg.setStringProperty("JMSXGroupID", channelId);
				distrubutor.send(queue, notificationMsg);
			}
		} catch (JMSException receivingEx) {
			log.error("incoming message processing failed: ", receivingEx);
		}
	}
}