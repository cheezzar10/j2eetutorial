package odin.j2ee.api;

import javax.ejb.Local;

import odin.j2ee.api.NotificationSubscription;

@Local
public interface NotificationSubscriptionRegistry {
	public String subscribe(Integer userId);
	
	public NotificationSubscription getSubscription(String subscriptionId);
	
	public void removeSubscription(String subscriptionId);

	public void dispatchNotification(Integer userId, String notification);
}
