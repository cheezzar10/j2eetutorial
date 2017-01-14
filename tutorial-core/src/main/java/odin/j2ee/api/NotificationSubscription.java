package odin.j2ee.api;

import javax.ejb.Local;

@Local
public interface NotificationSubscription {
	public String activate(Integer userId) throws SubscriptionActivationFailedException;
	
	public void deactivate();

	public String receive() throws ReceiversLimitExceededException, ReceivingFailedException;
}