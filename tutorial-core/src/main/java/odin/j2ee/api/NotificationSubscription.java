package odin.j2ee.api;

import javax.ejb.Local;

@Local
public interface NotificationSubscription {
	public String activate(Integer userId);
	
	public void deactivate();

	public Integer getUserId();
}