package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.MessageProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DnsRecordManager;

@Stateful(
		name = "DnsRecordManager",
		passivationCapable = false) // disables infinispan bean manager which causes immediate SFSB instances expiration
@StatefulTimeout(0)
@ExcludeDefaultInterceptors
public class DnsRecordManagerBean implements DnsRecordManager, SessionSynchronization {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final Set<Integer> removedRecIds = new HashSet<>();
	
	@Resource(mappedName = "java:jboss/DefaultJMSConnectionFactory")
	private ConnectionFactory connFactory;
	
	@Resource(mappedName = "java:/jms/queue/notifications")
	private Destination queue;
	
	private Connection connection;
	
	private Session session;

	@PostConstruct
	private void init() {
		log.debug("DNSRECMGR @{} instance created", hashCode());
	}

	@PreDestroy
	private void destroy() {
		log.debug("DNSRECMGR @{} instance destroyed", hashCode());
	}

	@Override
	public void removeRecord(int recId) {
		log.debug("DNSRECMGR @{} marking DNS record #{} as removed", hashCode(), recId);
		removedRecIds.add(recId);
	}

	@Override
	public void afterBegin() throws RemoteException {
		try {
			connection = connFactory.createConnection();
			session = connection.createSession();
		} catch (JMSException jmsEx) {
			throw new EJBException("JMS resources creation failed: ", jmsEx);
		}
		
		log.debug("DNSRECMGR @{} tx started", hashCode());
	}

	@Override
	public void afterCompletion(boolean committed) throws RemoteException {
		try {
			/* 
			 * not working code because it uses session which is associated with already inactive tx
			try (MessageProducer sender = session.createProducer(queue)) {
				TextMessage msg = session.createTextMessage("Closing JMS session");
				msg.setStringProperty("subscriptionId", "foo");
				sender.send(msg);
				
				log.debug("pre-close message was successfully sent");
			}
			*/
			
			session.close();
			connection.close();
			
			log.debug("JMS resources cleanup completed");
		} catch (JMSException jmsEx) {
			log.error("JMS resources cleanup failed: ", jmsEx);
		}
		
		log.debug("DNSRECMGR @{} tx {}", hashCode(), committed ? "committed" : "rolledback");
	}

	@Override
	public void beforeCompletion() throws RemoteException {
		log.debug("DNSRECMGR @{} performing DNS records deletion", hashCode());
		
		try (MessageProducer sender = session.createProducer(queue)) {
			for (Integer recId : removedRecIds) {
				log.debug("DNSRECMGR @{} removing DNS record #{}", hashCode(), recId);
				
				TextMessage msg = session.createTextMessage("DNS record " + recId + " removed");
				msg.setStringProperty("subscriptionId", "bar");
				sender.send(msg);
				log.debug("notification was sent about removed DNS record");
			}
		} catch (JMSException jmsEx) {
			log.error("failed to send DNS record removal notification: ", jmsEx);
		}
		
		
		log.debug("DNSRECMGR @{} {} DNS records deleted", hashCode(), removedRecIds);
	}
}
