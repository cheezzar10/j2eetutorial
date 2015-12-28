package odin.j2ee.ejb;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DispatchingFailedException;
import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;

@Stateful(name = "NotificationSubscription")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NotificationSubscriptionBean implements NotificationSubscription {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	private String subscriptionId;
	
	private Integer userId;
	
	private Session session;
	
	private CompletableFuture<Boolean> alive = null;

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
	public void dispatch(String notification) throws DispatchingFailedException {
		log.debug("dispatching notification {} to subscription {} using client connection {}", notification, subscriptionId, session.getId());
		
		long start = System.currentTimeMillis();
		RemoteEndpoint.Basic clientEndpoint = session.getBasicRemote();
		try {
			clientEndpoint.sendText(notification);
			log.debug("notification successfully dispatched in {} ms", System.currentTimeMillis() - start);
		} catch (IOException dispatchingFailedEx) {
			throw new DispatchingFailedException("failed to send notification", dispatchingFailedEx);
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

	@Override
	public Future<Boolean> isAlive() {
		ByteBuffer timeBuffer = ByteBuffer.allocate(Long.BYTES);
		timeBuffer.putLong(System.currentTimeMillis());
		try {
			session.getBasicRemote().sendPing(timeBuffer);
		} catch (IllegalArgumentException | IOException sendPingEx) {
			log.error("failed to send ping: ", sendPingEx);
			CompletableFuture<Boolean> nonAlive = new CompletableFuture<>();
			nonAlive.complete(false);
			return nonAlive;
		}
		alive = new CompletableFuture<>();
		return alive;
	}

	@Override
	public void markAsAlive() {
		log.debug("marking subscription {} connection {} as alive", subscriptionId, session.getId());
		if (alive != null) {
			alive.complete(true);
		} else {
			log.warn("failed to mark as alive - completion status uninitialized");
		}
	}
}
