package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscription;

public class UserSubscriptions {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Integer userId;
	private Map<String, NotificationSubscription> subscriptions = new HashMap<>();
	
	public UserSubscriptions(Integer userId) {
		this.userId = userId;
	}
	
	// TODO seems like lock is needed here, but check cause access to SFSB will be serialized anyway
	public void dispatch(String notification) {
		log.debug("dispatching notification to user {} subscriptions", userId);
		for (NotificationSubscription subscription : subscriptions.values()) {
			subscription.dispatch(notification);
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
