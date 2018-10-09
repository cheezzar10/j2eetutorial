package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.SessionContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.MessageProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.ClassicNotificationSender;
import odin.j2ee.api.DnsRecordManager;
import odin.j2ee.api.TxScopedManagerLocator;

@Stateless
public class ClassicNotificationSenderBean implements ClassicNotificationSender {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Resource(mappedName = "java:jboss/DefaultJMSConnectionFactory")
	private ConnectionFactory connFactory;
	
	@Resource(mappedName = "java:/jms/topic/notifications")
	private Destination notificationsTopic;
	
	@Resource
	private SessionContext sessionCtx;
	
	@EJB
	private TxScopedManagerLocator locator;
	
	@Override
	public void send(Integer userId, String msg) {
		log.debug("sending notification {} to user: {} using classic API", msg, userId);
		try (Connection conn = connFactory.createConnection(); Session session = conn.createSession()) {
			log.debug("JMS session class: {} instance @{}", session.getClass(), session.hashCode());
			
			MessageProducer sender = session.createProducer(notificationsTopic);
			TextMessage textMsg = session.createTextMessage(msg);
			textMsg.setIntProperty("userId", userId);
			sender.send(textMsg);
			log.debug("notification {} was successfully sent to user: {}", msg, userId);
		} catch (JMSException sendingFailedEx) {
			throw new IllegalStateException("failed to send notification: ", sendingFailedEx);
		}
		
		// sessionCtx.setRollbackOnly();
	}
	
	public void sendMessages(Integer userId, String[] msgs) {
		for (int i = 0;i < msgs.length;++i) {
			String msg = msgs[i];
			send(userId, msg);
			
			try {
				log.debug("sleeping before next send attempt");
				Thread.sleep(100);
				
				DnsRecordManager dnsRecMgr = locator.getManager(DnsRecordManager.class);
				dnsRecMgr.removeRecord(1);
				
				if (i > 1) {
					// check that stateful scoped resources will be released after throw
					throw new IllegalStateException("boom");
				}
			} catch(InterruptedException intrEx) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
}