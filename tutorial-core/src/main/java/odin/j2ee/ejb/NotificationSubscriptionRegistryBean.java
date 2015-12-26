package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
	
	@Resource
	private SessionContext sessionCtx;
	
	private final AtomicBoolean dispatching = new AtomicBoolean();
	
	@Override
	@Lock(LockType.WRITE)
	public String subscribe(Integer userId) {
		log.debug("creating notification subscription for user #{}", userId);
		
		NotificationSubscription subscription = createSubscription();
		String subscriptionId = subscription.activate(userId);
		subscriptions.put(subscriptionId, subscription);
		
		return subscriptionId;
	}
	
	@Override
	@Lock(LockType.READ)
	public void dispatch() {
		if (!dispatching.compareAndSet(false, true)) {
			log.debug("pending notifications dispatching in progress");
		}
		
		Set<Map.Entry<String, NotificationSubscription>> entries = subscriptions.entrySet();
		log.debug("dispatching pending notifications to {} active subscriptions", entries.size());
		for (Map.Entry<String, NotificationSubscription> entry : entries) {
			String subscriptionId = entry.getKey();
			NotificationSubscription subscription = entry.getValue();
			int dispatched = subscription.dispatch();
			log.debug("{} pending notifications dispatched for subscription: {}", dispatched, subscriptionId);
		}
		
		dispatching.set(false);
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
		subscriptions.remove(subscriptionId);
	}
	
	private NotificationSubscription createSubscription() {
		NotificationSubscription subscription = (NotificationSubscription)sessionCtx.lookup("ejb/NotificationSubscription");
		return subscription;
	}
}
