package odin.j2ee;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
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
	
	@OnMessage
	public void onCommand(Session session, String command) {
		log.debug("command {} received via connection  {}", command, session.getId());
		if ("unsubscribe".equals(command)) {
			NotificationSubscription subscription = registry.getSubscription(subscriptionId);
			subscription.deactivate();
			try {
				session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "client unsubscribed"));
			} catch (IOException closeFailedEx) {
				log.error("failed to close client connetion: ", closeFailedEx);
			}
		} else {
			log.debug("unknow command: '{}'", command);
		}
	}
	
	@OnError
	public void errorHappened(Throwable error) {
		log.error(String.format("websocket connection %s error", session.getId()), error);
	}
	
	@OnClose
	public void onClose() {
		log.debug("websocket connection {} closed", session.getId());
		// TODO unplug client connection
	}
}