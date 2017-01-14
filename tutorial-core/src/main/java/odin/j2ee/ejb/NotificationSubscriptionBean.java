package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;
import odin.j2ee.api.ReceiversLimitExceededException;
import odin.j2ee.api.ReceivingFailedException;
import odin.j2ee.api.SubscriptionActivationFailedException;

@Stateful(name = "NotificationSubscription")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class NotificationSubscriptionBean implements NotificationSubscription {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final int RECEIVERS_LIMIT = 2;
	
	private static final AtomicInteger receivers = new AtomicInteger(0);
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	private String id;
	
	@Resource(mappedName = "java:/jms/topic/notifications")
	private Topic topic;
	
	@Resource(mappedName = "java:jboss/DefaultJMSConnectionFactory")
	private ConnectionFactory connFactory;
	
	@PostConstruct
	private void init() {
		log.debug("new notification subscription instance @{} initialized", hashCode());
	}
	
	@Override
	public String activate(Integer userId) throws SubscriptionActivationFailedException {
		log.debug("activating user #{} notification subscription", userId);
		id = String.valueOf(System.currentTimeMillis());
		try (Connection conn = connFactory.createConnection(); Session session = conn.createSession(); MessageConsumer receiver = session.createDurableConsumer(topic, id)) {
			log.debug("shared durable JMS subscription {} created", id);
		} catch (JMSException jmsEx) {
			throw new SubscriptionActivationFailedException(id, jmsEx);
		}
		log.debug("user {} notification subscription {} activated", userId, id);
		return id;
	}
	
	@Override
	@Remove
	public void deactivate() {
		log.debug("deactivating notification subscription {}", id);
		try (Connection conn = connFactory.createConnection(); Session session = conn.createSession()) {
			session.unsubscribe(id);
			log.debug("shared durable JMS subscription {} deactivated", id);
		} catch (JMSException unsubscribingEx) {
			throw new IllegalStateException("failed to deactivate subscription: ", unsubscribingEx);
		}
		registry.removeSubscription(id);
	}

	@Override
	public String receive() throws ReceiversLimitExceededException, ReceivingFailedException {
		log.debug("trying to receive notification using subscription: {}", id);
		
		int activeReceivers = receivers.get();
		if (activeReceivers >= RECEIVERS_LIMIT) {
			throw new ReceiversLimitExceededException(RECEIVERS_LIMIT);
		}
		
		receivers.incrementAndGet();
		try (Connection conn = connFactory.createConnection(); Session session = conn.createSession(); MessageConsumer receiver = session.createDurableConsumer(topic, id)) {
			conn.start();
			TextMessage notificationMsg = (TextMessage)receiver.receive(10000);
			if (notificationMsg != null) {
				String notification = notificationMsg.getText();
				log.debug("notification {} was successfully received", notification);
				return notification;
			} else {
				return "no notifications";
			}
		} catch (JMSException receivingEx) {
			throw new ReceivingFailedException(id, receivingEx);
		} finally {
			receivers.decrementAndGet();
		}
	}
}
