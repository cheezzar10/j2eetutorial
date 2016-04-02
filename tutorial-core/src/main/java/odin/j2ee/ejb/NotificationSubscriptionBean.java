package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

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

@Stateful(name = "NotificationSubscription")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class NotificationSubscriptionBean implements NotificationSubscription {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	private String id;
	
	private Integer userId;
	
	@Resource(mappedName = "java:/jms/topic/notifications")
	private Topic topic;
	
	@Resource(mappedName = "java:jboss/DefaultJMSConnectionFactory")
	private ConnectionFactory connFactory;
	
	@PostConstruct
	private void init() {
		log.debug("new notification subscription instance @{} initialized", hashCode());
	}
	
	@Override
	public String activate(Integer userId) {
		log.debug("activating user #{} notification subscription", userId);
		id = String.valueOf(System.currentTimeMillis());
		this.userId = userId;
		try (Connection conn = connFactory.createConnection(); Session session = conn.createSession(); MessageConsumer receiver = session.createDurableConsumer(topic, id)) {
			log.debug("shared durable JMS subscription {} created", id);
		} catch (JMSException sendingFailedEx) {
			throw new IllegalStateException("subscription activation failed: ", sendingFailedEx);
		}
		log.debug("user {} notification subscription {} activated", userId, id);
		return id;
	}
	
	@Override
	public Integer getUserId() {
		return userId;
	}
	
	@Override
	@Remove
	public void deactivate() {
		log.debug("deactivating notification subscription {}", id);
		try (Connection conn = connFactory.createConnection(); Session session = conn.createSession()) {
			session.unsubscribe(id);
			log.debug("shared durable JMS subscription {} deactivated", id);
		} catch (JMSException sendingFailedEx) {
			throw new IllegalStateException("failed to deactivate subscription: ", sendingFailedEx);
		}
		registry.removeSubscription(id);
	}

	@Override
	public String receive() {
		log.debug("trying to receive notification using subscription: {}", id);
		// TODO migrate to shared durable consumer
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
		} catch (JMSException sendingFailedEx) {
			throw new IllegalStateException("failed to receive notification: ", sendingFailedEx);
		}
	}
}
