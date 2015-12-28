package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
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
	
	private static final int DISTRIBUTION_QUEUES_MAX = 8;
	
	@Resource
	private MessageDrivenContext context;
		
	@Inject
	private JMSContext jmsCtx;
	
	private Destination[] distributionQueues;
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@PostConstruct
	private void init() {
		List<? extends Destination> queues = new LinkedList<>();
		for (int queueIdx=0;queueIdx<DISTRIBUTION_QUEUES_MAX;queueIdx++) {
			try {
				context.lookup(String.format("queue/notification%02d", queueIdx));
			} catch (IllegalArgumentException queueNotFound) {
				log.debug("distribution queue with index = {} not found", queueIdx);
				break;
			}
		}
		distributionQueues = queues.toArray(new Destination[queues.size()]);
		log.debug("{} distribution queues configured", distributionQueues.length);
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
			
			JMSProducer broker = jmsCtx.createProducer();
			Set<String> userChannelIds = registry.getUserChannelIds(userId);
			for (String channelId : userChannelIds) {
				int channelHash = Math.abs(channelId.hashCode());
				int queueIdx = channelHash % distributionQueues.length;
				log.debug("dispatching message {} to channel {} using distribution queue #{}", textMsg.getText(), channelId, queueIdx);
				
				TextMessage notificationMsg = jmsCtx.createTextMessage(textMsg.getText());
				notificationMsg.setStringProperty("subscriptionId", channelId);
				broker.send(distributionQueues[queueIdx], notificationMsg);
			}
		} catch (JMSException receivingEx) {
			log.error("incoming message processing failed: ", receivingEx);
		}
	}
}