package odin.j2ee.api;

import javax.ejb.Local;
import javax.websocket.Session;

@Local
public interface NotificationSubscription {
	public String activate(Integer userId);
	
	public int dispatch();
	
	public void registerConnection(Session session);
	
	public void deactivate();
}