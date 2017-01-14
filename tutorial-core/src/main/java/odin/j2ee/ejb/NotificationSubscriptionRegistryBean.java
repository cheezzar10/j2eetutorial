package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DispatchingFailedException;
import odin.j2ee.api.NotificationChannel;
import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;

@Singleton(name = "NotificationSubscriptionRegistry")
@EJB(name = "ejb/NotificationSubscription", beanInterface = NotificationSubscription.class)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class NotificationSubscriptionRegistryBean implements NotificationSubscriptionRegistry {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Map<String, NotificationChannel> subscriptions = new ConcurrentHashMap<>();
	private Map<Integer, UserSubscriptions> userSubscriptions = new HashMap<>();
	
	private BidiMap<String, WeakReference<NotificationSubscription> > subscriptionRefs = new DualHashBidiMap<>();
	
	private ReferenceQueue<NotificationSubscription> subscriptionRefsQueue = new ReferenceQueue<>();
	
	@Resource
	private SessionContext sessionCtx;
	
	@Override
	@Lock(LockType.WRITE)
	public String subscribe(Integer userId) {
		log.debug("creating notification subscription for user #{}", userId);
		
		NotificationSubscription subscription = createSubscription();
		String subscriptionId = subscription.activate(userId);
		NotificationChannel channel = new NotificationChannel(subscription, subscriptionId);
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
	@Lock(LockType.WRITE)
	public void registerSubscription(String subscriptionId, NotificationSubscription subscription) {
		log.debug("registering notification subscription reference @{}", subscription.hashCode());
		
		subscriptionRefs.computeIfAbsent(subscriptionId, (sid) -> {
			return new WeakReference<>(subscription, subscriptionRefsQueue);
		});
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
		NotificationSubscription subscription = channel.getSubscription();
		if (subscription != null) {
			Integer userId = subscription.getUserId();
			log.debug("removing subscription {} from user {} subscriptions list", subscriptionId, userId);
			UserSubscriptions userSubs = userSubscriptions.get(userId);
			userSubs.remove(channel.getId());
			if (userSubs.isEmpty()) {
				log.debug("subscription {} was the last user {} subscription", subscriptionId, userId);
				userSubscriptions.remove(userId);
			}
		} else {
			log.debug("detected attempt to remove unknown subscription {}", subscriptionId);
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

	@Override
	@Lock(LockType.WRITE)
	public void removeDeactivatedSubscriptions() {
		log.debug("performing deactivated channels cleanup");
		Reference<? extends NotificationSubscription> ref = subscriptionRefsQueue.poll();
		BidiMap<WeakReference<NotificationSubscription>, String> subscriptionIds = subscriptionRefs.inverseBidiMap();
		int cleanedUp = 0;
		while (ref != null) {
			String deactivatedSubscriptionId = subscriptionIds.remove(ref);
			subscriptionRefs.remove(deactivatedSubscriptionId);
			removeSubscription(deactivatedSubscriptionId);
			cleanedUp++;
			ref = subscriptionRefsQueue.poll();
		}
		log.debug("cleaned up {} deactivated subscriptions", cleanedUp);
	}
	
	@Schedule(hour = "*", minute = "*/1", persistent = false)
	private void removeDeactivatedsubscriptionsPeriodically() {
		sessionCtx.getBusinessObject(NotificationSubscriptionRegistry.class).removeDeactivatedSubscriptions();
	}
}
