package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
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
	
	@Inject
	private JMSContext jmsCtx;
	
	@Resource(mappedName = "java:/jms/topic/notifications")
	private Topic topic;
	
	@PostConstruct
	private void init() {
		log.debug("new notification subscription instance @{} initialized", hashCode());
	}
	
	@Override
	public String activate(Integer userId) {
		log.debug("activating user #{} notification subscription", userId);
		id = String.valueOf(System.currentTimeMillis());
		this.userId = userId;
		jmsCtx.createSharedDurableConsumer(topic, id);
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
		jmsCtx.unsubscribe(id);
		registry.removeSubscription(id);
	}

	@Override
	public String receive() {
		log.debug("trying to receive notification using subscription: {}", id);
		JMSConsumer receiver = jmsCtx.createSharedDurableConsumer(topic, id);
		String notification = receiver.receiveBody(String.class, 10000);
		if (notification != null) {
			log.debug("notification {} was successfully received", notification);
			return notification;
		} else {
			return "no notifications";
		}
	}
}
