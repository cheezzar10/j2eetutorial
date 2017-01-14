package odin.j2ee.api;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationChannel {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private AtomicReference<Session> session = new AtomicReference<>();
	
	private final NotificationSubscription subscription;
	
	private final String id;
	
	private final Integer userId;
	
	private volatile CompletableFuture<Boolean> ready;
	
	public NotificationChannel(NotificationSubscription subscription, String id, Integer userId) {
		this.subscription = subscription;
		this.id = id;
		this.userId = userId;
	}
	
	public String getId() {
		return id;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void dispatch(String notification) throws DispatchingFailedException {
		log.debug("dispatching notification using channel {} plugged to connection {}", id, session.get().getId());
		
		long start = System.currentTimeMillis();
		RemoteEndpoint.Basic clientEndpoint = session.get().getBasicRemote();
		try {
			clientEndpoint.sendText(notification);
			log.debug("notification successfully dispatched in {} ms", System.currentTimeMillis() - start);
		} catch (IOException dispatchingFailedEx) {
			throw new DispatchingFailedException("failed to send notification", dispatchingFailedEx);
		}
	}
	
	public NotificationSubscription getSubscription() {
		return subscription;
	}

	public void connect(Session session) {
		log.debug("connecting notification subscription {} channel to connection {}", id, session.getId());
		this.session.set(session);
	}

	public void setReady(boolean status) {
		log.debug("changing notification subscription {} channel readiness status to: {}", id, status);
		
		if (ready != null) {
			ready.complete(status);
		} else {
			log.warn("failed to mark as ready - completion status uninitialized");
		}
	}

	public boolean isReady() {
		ByteBuffer timeBuffer = ByteBuffer.allocate(Long.BYTES);
		timeBuffer.putLong(System.currentTimeMillis());
		try {
			session.get().getBasicRemote().sendPing(timeBuffer);
		} catch (IllegalArgumentException | IOException sendPingEx) {
			log.error("ping failed - connection not ready");
			return false;
		}
		
		ready = new CompletableFuture<>();
		try {
			return ready.get(5, TimeUnit.SECONDS);
		} catch (TimeoutException timeoutEx) {
			log.debug("subscription {} client connection status check failed - timeout expired", id);
			return false;
		} catch (ExecutionException execEx) {
			log.debug("client connection status check failed: ", execEx);
			return false;
		} catch (InterruptedException ineterruptedEx) {
			log.debug("interrupted while checking subscription {} client connection status", id);
			Thread.currentThread().interrupt();
			return false;
		}
	}
}
