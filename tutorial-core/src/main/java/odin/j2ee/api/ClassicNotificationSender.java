package odin.j2ee.api;

import javax.ejb.Remote;

@Remote
public interface ClassicNotificationSender {
	void send(Integer userId, String msg);
	
	void sendMessages(Integer userId, String[] msgs);
}