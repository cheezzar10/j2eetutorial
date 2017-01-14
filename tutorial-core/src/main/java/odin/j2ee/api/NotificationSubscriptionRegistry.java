package odin.j2ee.api;

import java.util.Set;

import javax.ejb.Local;

@Local
public interface NotificationSubscriptionRegistry {
	public String subscribe(Integer userId) throws SubscriptionActivationFailedException;
	
	public NotificationChannel getSubscription(String subscriptionId);
	
	public void removeSubscription(String subscriptionId);

	public Set<String> getUserChannelIds(Integer userId);

	public void dispatchNotification(String subscriptionId, String text) throws DispatchingFailedException;
}
