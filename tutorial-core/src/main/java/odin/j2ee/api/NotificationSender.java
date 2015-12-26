package odin.j2ee.api;

import javax.ejb.Local;

@Local
public interface NotificationSender {
	public void send(Integer userId, String msg);
}