package odin.j2ee;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationChannel;
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
		
		NotificationChannel subscription = registry.getSubscription(subscriptionId);
		if (subscription != null) {
			subscription.connect(session);
		} else {
			closeSession(new CloseReason(CloseCodes.VIOLATED_POLICY, "subscription not found"));
		}
	}
	
	@OnMessage
	public void onCommand(String command) {
		log.debug("command {} received via connection {}", command, session.getId());
		if ("unsubscribe".equals(command)) {
			unsubscribe();
			closeSession(new CloseReason(CloseCodes.NORMAL_CLOSURE, "client unsubscribed"));
			
		} else {
			log.debug("unknow command: '{}'", command);
		}
	}
	
	@OnMessage
	public void catchPong(PongMessage pongMsg) {
		log.debug("pong message received using connection: {}", session.getId());
		NotificationChannel channel = registry.getSubscription(subscriptionId);
		channel.setReady(true);
	}

	private void closeSession(CloseReason reason) {
		try {
			session.close();
		} catch (IOException closeFailedEx) {
			log.error("failed to close client connection: ", closeFailedEx);
		}
	}

	private void unsubscribe() {
		NotificationChannel channel = registry.getSubscription(subscriptionId);
		NotificationSubscription subscription = channel.getSubscription();
		subscription.deactivate();
	}
	
	@OnError
	public void errorHappened(Throwable error) {
		log.error(String.format("websocket connection %s error: ", session.getId()), error);
	}
	
	@OnClose
	public void onClose(CloseReason reason) {
		CloseReason.CloseCode closeCode = reason.getCloseCode();
		log.debug("websocket connection {} closed. close code: {} reason: {}", session.getId(), 
				closeCode.getCode(), reason.getReasonPhrase());
		
		if (closeCode == CloseCodes.NORMAL_CLOSURE || closeCode == CloseCodes.GOING_AWAY) {
			unsubscribe();
		}
	}
}