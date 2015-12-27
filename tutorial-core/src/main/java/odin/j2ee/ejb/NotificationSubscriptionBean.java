package odin.j2ee.ejb;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;

@Stateful(name = "NotificationSubscription")
public class NotificationSubscriptionBean implements NotificationSubscription {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	private String subscriptionId;
	
	private Integer userId;
	
	private Session session;

	@PostConstruct
	private void init() {
		log.debug("new notification subscription instance @{} initialized", hashCode());
	}
	
	@Override
	public String activate(Integer userId) {
		log.debug("activating user #{} notification subscription", userId);
		subscriptionId = String.valueOf(System.currentTimeMillis());
		this.userId = userId;
		log.debug("user {} subscription {} activated", userId, subscriptionId);
		return subscriptionId;
	}
	
	@Override
	public String getId() {
		return subscriptionId;
	}
	
	@Override
	public Integer getUserId() {
		return userId;
	}
	
	@Override
	public void dispatch(String notification) {
		log.debug("dispatching notification received from subscription {} using client connection {}", subscriptionId, session.getId());
		
		long start = System.currentTimeMillis();
		RemoteEndpoint.Basic clientEndpoint = session.getBasicRemote();
		try {
			clientEndpoint.sendText(notification);
			log.debug("notification successfully dispatched in {} ms", System.currentTimeMillis() - start);
		} catch (IOException sendingFailedEx) {
			log.debug("failed to dispatch notification to client connection", sendingFailedEx);
		}
	}
	
	@Override
	public void attachConnection(Session session) {
		log.debug("attaching client connection {} to subscription: {}", session.getId(), subscriptionId);
		this.session = session;
	}
	
	@Override
	@Remove
	public void deactivate() {
		log.debug("deactivating notification subscription {}", subscriptionId);
		registry.removeSubscription(subscriptionId);
	}
}
