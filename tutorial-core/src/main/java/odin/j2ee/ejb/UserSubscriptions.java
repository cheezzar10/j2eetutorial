package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DispatchingFailedException;
import odin.j2ee.api.NotificationSubscription;

public class UserSubscriptions {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Integer userId;
	// TODO NotificationSubscriptionHandle
	private Map<String, NotificationSubscription> subscriptions = new HashMap<>();
	
	public UserSubscriptions(Integer userId) {
		this.userId = userId;
	}
	
	// TODO seems like lock is needed here, but check cause access to SFSB will be serialized anyway
	public void dispatch(String notification) throws DispatchingFailedException {
		log.debug("dispatching notification to user {} subscriptions", userId);
		for (NotificationSubscription subscription : subscriptions.values()) {
			try {
				boolean alive = subscription.isAlive().get(5, TimeUnit.SECONDS);
				if (alive) {
					subscription.dispatch(notification);
				}
			} catch (TimeoutException timeoutEx) {
				log.debug("subscription {} client connection status check failed - timeout expired", subscription.getId());
				throw new DispatchingFailedException("client not responding", timeoutEx);
			} catch (ExecutionException statusCheckEx) {
				throw new DispatchingFailedException("client connection status check failed", statusCheckEx);
			} catch (InterruptedException interruptedEx) {
				log.debug("interrupted while checking subscription {} client connection status", subscription.getId());
				Thread.currentThread().interrupt();
			}
		}
	}

	public void add(NotificationSubscription subscription) {
		log.debug("adding subscription {} to user {} subscriptions list", subscription.getId(), userId);
		subscriptions.put(subscription.getId(), subscription);
	}

	public void remove(String subscriptionId) {
		log.debug("removing subscription {} from user {} subscriptions list", subscriptionId, userId);
		subscriptions.remove(subscriptionId);
	}

	public boolean isEmpty() {
		return subscriptions.isEmpty();
	}
}
