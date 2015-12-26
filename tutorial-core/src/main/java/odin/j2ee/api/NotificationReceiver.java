package odin.j2ee.api;

import javax.ejb.Local;

@Local
public interface NotificationReceiver {
	public String receive(Integer userId);
	
	public String unsubscribe();
}