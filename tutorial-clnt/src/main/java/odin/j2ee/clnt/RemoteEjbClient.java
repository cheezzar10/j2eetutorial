package odin.j2ee.clnt;

import javax.transaction.UserTransaction;

import org.jboss.ejb.client.EJBClient;

import odin.j2ee.api.ClassicNotificationSender;

public class RemoteEjbClient {
	public static void main(String[] args) throws Exception {
		UserTransaction tx = EJBClient.getUserTransaction("nodename");
		
		// lookup sender
		ClassicNotificationSender sender = lookup("", ClassicNotificationSender.class);
		
		tx.commit();
	}

	private <T> T lookup(String jndiName, Class<T> clazz) {
		return null;
	}
}
