package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;

@Singleton(name = "NotificationSubscriptionRegistry")
@EJB(name = "ejb/NotificationSubscription", beanInterface = NotificationSubscription.class)
public class NotificationSubscriptionRegistryBean implements NotificationSubscriptionRegistry {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Map<String, NotificationSubscription> subscriptions = new ConcurrentHashMap<>();
	private Map<Integer, UserSubscriptions> userSubscriptions = new HashMap<>(); 
	
	@Resource
	private SessionContext sessionCtx;
	
	@Override
	@Lock(LockType.WRITE)
	public String subscribe(Integer userId) {
		log.debug("creating notification subscription for user #{}", userId);
		
		NotificationSubscription subscription = createSubscription();
		String subscriptionId = subscription.activate(userId);
		subscriptions.put(subscriptionId, subscription);
		UserSubscriptions userSubs = userSubscriptions.get(userId);
		if (userSubs == null) {
			userSubs = new UserSubscriptions(userId);
			userSubscriptions.put(userId, userSubs);
		}
		userSubs.add(subscription);
		
		return subscriptionId;
	}
	
	@Override
	@Lock(LockType.READ)
	public NotificationSubscription getSubscription(String subscriptionId) {
		log.debug("searching for notification subscription {}", subscriptionId);
		NotificationSubscription subscription = subscriptions.get(subscriptionId);
		
		if (subscription == null) {
			log.debug("notification subscription {} not found", subscriptionId);
		}
		
		return subscription;
	}
	
	@Override
	@Lock(LockType.WRITE)
	public void removeSubscription(String subscriptionId) {
		log.debug("removing notification subscription {}", subscriptionId);
		NotificationSubscription subscription = subscriptions.remove(subscriptionId);
		if (subscription != null) {
			Integer userId = subscription.getUserId();
			log.debug("removing subscription {} from user {} subscriptions list", subscriptionId, userId);
			UserSubscriptions userSubs = userSubscriptions.get(userId);
			userSubs.remove(subscription.getId());
			if (userSubs.isEmpty()) {
				log.debug("subscription {} is the last user {} subscription", subscriptionId, userId);
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
	public void dispatchNotification(Integer userId, String notification) {
		log.debug("dispatching notification {} to subscriptions associated with user: {}", notification, userId);
		UserSubscriptions userSubs = userSubscriptions.get(userId);
		if (userSubs != null) {
			userSubs.dispatch(notification);
		}
	}
}
