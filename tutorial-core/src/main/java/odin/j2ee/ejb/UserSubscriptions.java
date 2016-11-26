package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DispatchingFailedException;
import odin.j2ee.api.NotificationChannel;

// TODO rename to SubscriptionChannels
public class UserSubscriptions {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Integer userId;
	private Map<String, NotificationChannel> subscriptions = new HashMap<>();
	
	public UserSubscriptions(Integer userId) {
		this.userId = userId;
	}
	
	// TODO seems like lock is needed here, but check cause access to SFSB will be serialized anyway
	public void dispatch(String notification) throws DispatchingFailedException {
		log.debug("dispatching notification to user {} subscriptions", userId);
		for (NotificationChannel channel : subscriptions.values()) {
			boolean ready = channel.isReady();
			if (ready) {
				channel.dispatch(notification);
			}
		}
	}

	public void add(NotificationChannel channel) {
		log.debug("adding subscription {} to user {} subscriptions list", channel.getId(), userId);
		subscriptions.put(channel.getId(), channel);
	}

	public void remove(String subscriptionId) {
		log.debug("removing subscription {} from user {} subscriptions list", subscriptionId, userId);
		subscriptions.remove(subscriptionId);
	}

	public boolean isEmpty() {
		return subscriptions.isEmpty();
	}

	public Set<String> getIds() {
		return Collections.unmodifiableSet(subscriptions.keySet());
	}
}
