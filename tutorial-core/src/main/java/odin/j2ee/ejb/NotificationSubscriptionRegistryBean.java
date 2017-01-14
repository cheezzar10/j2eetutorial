package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DispatchingFailedException;
import odin.j2ee.api.NotificationChannel;
import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;
import odin.j2ee.api.SubscriptionActivationFailedException;

@Singleton(name = "NotificationSubscriptionRegistry")
@EJB(name = "ejb/NotificationSubscription", beanInterface = NotificationSubscription.class)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class NotificationSubscriptionRegistryBean implements NotificationSubscriptionRegistry {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Map<String, NotificationChannel> subscriptions = new ConcurrentHashMap<>();
	private Map<Integer, UserSubscriptions> userSubscriptions = new HashMap<>();
	
	@Resource
	private SessionContext sessionCtx;
	
	@Override
	@Lock(LockType.WRITE)
	public String subscribe(Integer userId) throws SubscriptionActivationFailedException {
		log.debug("creating notification subscription for user #{}", userId);
		
		NotificationSubscription subscription = createSubscription();
		String subscriptionId = subscription.activate(userId);
		NotificationChannel channel = new NotificationChannel(subscription, subscriptionId, userId);
		subscriptions.put(subscriptionId, channel);
		UserSubscriptions userSubs = userSubscriptions.get(userId);
		if (userSubs == null) {
			userSubs = new UserSubscriptions(userId);
			userSubscriptions.put(userId, userSubs);
		}
		userSubs.add(channel);
		return subscriptionId;
	}
	
	@Override
	@Lock(LockType.READ)
	public NotificationChannel getSubscription(String subscriptionId) {
		log.debug("searching for notification subscription {}", subscriptionId);
		NotificationChannel subscriptionRef = subscriptions.get(subscriptionId);
		if (subscriptionRef == null) {
			log.debug("notification subscription {} not found", subscriptionId);
		}
		return subscriptionRef;
	}
	
	@Override
	@Lock(LockType.WRITE)
	public void removeSubscription(String subscriptionId) {
		log.debug("removing notification subscription {}", subscriptionId);
		NotificationChannel channel = subscriptions.remove(subscriptionId);
		Integer userId = channel.getUserId();
		UserSubscriptions userSubs = userSubscriptions.get(userId);
		userSubs.remove(channel.getId());
		if (userSubs.isEmpty()) {
			log.debug("subscription {} was the last user {} subscription", subscriptionId, userId);
			userSubscriptions.remove(userId);
		}
	}
	
	private NotificationSubscription createSubscription() {
		NotificationSubscription subscription = (NotificationSubscription)sessionCtx.lookup("ejb/NotificationSubscription");
		return subscription;
	}
	
	@Override
	@Lock(LockType.READ)
	public void dispatchNotification(String subscriptionId, String notification) throws DispatchingFailedException {
		NotificationChannel channel = subscriptions.get(subscriptionId);
		if (channel != null) {
			channel.dispatch(notification);
		}
	}

	@Override
	@Lock(LockType.READ)
	public Set<String> getUserChannelIds(Integer userId) {
		log.debug("loading all channels for user {} subscriptions", userId);
		UserSubscriptions channels = userSubscriptions.get(userId);
		if (channels != null) {
			return channels.getIds();
		}
		return Collections.emptySet();
	}
}
