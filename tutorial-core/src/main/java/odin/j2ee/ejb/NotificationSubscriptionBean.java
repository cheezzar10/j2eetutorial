package odin.j2ee.ejb;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;

@Stateful(name = "NotificationSubscription")
public class NotificationSubscriptionBean implements NotificationSubscription {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private JMSContext jmsCtx;
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@Resource(mappedName = "java:/jms/topic/notifications")
	private Topic topic;
	
	private String subscriptionId;
	
	private String selector;
	
	private Session session;

	@PostConstruct
	private void init() {
		log.debug("new notification subscription instance @{} initialized", hashCode());
	}
	
	@Override
	public String activate(Integer userId) {
		log.debug("activating user #{} notification subscription", userId);
		
		subscriptionId = String.valueOf(System.currentTimeMillis());
		selector = String.format("userId = %d", userId);
		
		// TODO JMSContext clientId before subscribing
		log.debug("creating new JMS subscription {} with selector: {}", subscriptionId, selector);
		jmsCtx.createSharedDurableConsumer(topic, subscriptionId, selector);
		return subscriptionId;
	}
	
	@Override
	public int dispatch() {
		log.debug("dispatching pending notifications for subscription: {}", subscriptionId);
		
		int dispatched = 0;
		RemoteEndpoint.Basic clientEndpoint = session.getBasicRemote();
		
		JMSConsumer subscriber = jmsCtx.createSharedDurableConsumer(topic, subscriptionId, selector);
		String notification = subscriber.receiveBodyNoWait(String.class);
		while (notification != null) {
			// TODO may be batching will be appropriate here
			try {
				clientEndpoint.sendText(notification);
			} catch (IOException sendingFailedEx) {
				log.debug("failed to dispatch notification to client connection", sendingFailedEx);
			}
			dispatched++;
			notification = subscriber.receiveBodyNoWait(String.class);
		}
		
		return dispatched;
	}
	
	@Override
	public void registerConnection(Session session) {
		log.debug("attaching connection {} to notification subscription: {}", session.getId(), subscriptionId);
		this.session = session;
	}
	
	@Override
	@Remove
	public void deactivate() {
		log.debug("deactivating user #{} notification subscription");
		jmsCtx.unsubscribe(subscriptionId);
		registry.removeSubscription(subscriptionId);
	}
}
