package odin.j2ee;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;

// TODO rename to NotificationSubscriptionEndpoint
@ServerEndpoint("/notifications/subscription/{subscriptionId}")
public class NotificationDispatchingEndpoint  {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Session session;
	
	private String subscriptionId;
	
	@Inject
	private NotificationSubscriptionRegistry registry;
	
	@OnOpen
	public void connect(Session session, @PathParam("subscriptionId") String subscriptionId) {
		log.debug("linking websocket connection {} with notification subscription: {}", session.getId(), subscriptionId);
		this.session = session;
		this.subscriptionId = subscriptionId;
		
		NotificationSubscription subscription = registry.getSubscription(subscriptionId);
		subscription.attachConnection(session);
	}
	
	@OnError
	public void errorHappened(Throwable error) {
		log.error(String.format("websocket connection %s error", session.getId()), error);
	}
	
	@OnClose
	public void onClose() {
		log.debug("websocket connection {} closed", session.getId());
		
		NotificationSubscription subscription = registry.getSubscription(subscriptionId);
		subscription.deactivate();
	}
}