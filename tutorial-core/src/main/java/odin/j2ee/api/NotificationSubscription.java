package odin.j2ee.api;

import javax.ejb.Local;
import javax.websocket.Session;

@Local
public interface NotificationSubscription {
	public String activate(Integer userId);
	
	public String getId();
	
	public void dispatch(String notification) throws DispatchingFailedException;
	
	public void attachConnection(Session session);
	
	public void deactivate();

	public Integer getUserId();
}