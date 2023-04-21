package odin.j2ee.clnt;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.jboss.ejb.client.EJBClient;

import odin.j2ee.api.ClassicNotificationSender;

public class RemoteEjbClient {
	private static final String NOTIF_SENDER_JNDI_NAME = "ejb:tutorial-app/tutorial-core/ClassicNotificationSender!odin.j2ee.api.ClassicNotificationSender";

	public static void main(String[] args) throws Exception {
		Properties namingProps = new Properties();
		namingProps.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"org.wildfly.naming.client.WildFlyInitialContextFactory");
		namingProps.setProperty(Context.PROVIDER_URL, "remote+http://localhost:8080");

		Context namingCtx = new InitialContext(namingProps);

		for (int i = 0; i < 1; ++i) {
			sendNotifications(i, namingCtx);
			sendNotificationsInTx(i, namingCtx);
		}
	}

	private static void sendNotifications(int attempt, Context namingCtx) throws Exception {
		// lookup sender
		ClassicNotificationSender sender = lookup(namingCtx, NOTIF_SENDER_JNDI_NAME, ClassicNotificationSender.class);

		try {
			// sender.sendMessages(1, new String[] { "foo" });
			// sender.sendMessages(2, new String[] { "foo", "bar" });
			sender.sendMessages(3, new String[] { "foo", "bar", "baz" });
			System.out.printf("send notification attempt #%d completed successfully%n", attempt);
		} catch (Exception ex) {
			System.out.printf("send notification attempt #%d failed: '%s'%n", attempt, ex.getMessage());
		}
	}

	private static void sendNotificationsInTx(int txId, Context namingCtx) throws Exception {
		UserTransaction tx = EJBClient.getUserTransaction("oss-dev-osx");
		tx.begin();

		// lookup sender
		ClassicNotificationSender sender = lookup(namingCtx, NOTIF_SENDER_JNDI_NAME, ClassicNotificationSender.class);

		try {
			sender.sendMessages(1, new String[] { "foo" });
			sender.sendMessages(2, new String[] { "foo", "bar" });
			// sender.sendMessages(3, new String[] { "foo", "bar", "baz" });
			tx.commit();
			System.out.printf("tx %d committed%n", txId);
		} catch (Exception ex) {
			tx.rollback();
			System.out.printf("tx %d rolled back%n", txId);
		}
	}

	private static <T> T lookup(Context namingCtx, String name, Class<T> iface) throws NamingException {
		return iface.cast(namingCtx.lookup(name));
	}
}
