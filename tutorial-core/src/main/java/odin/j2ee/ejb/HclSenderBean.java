package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.HclSender;

@Stateless
public class HclSenderBean implements HclSender {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	// @Resource(lookup = "java:/jms/hcl-replicator-conn")
	private ConnectionFactory connFactory;
	
	@Override
	public void sendHcl(String hcl) {
		log.debug("sending hcl: {}", hcl);
		try (Connection conn = connFactory.createConnection(); Session session = conn.createSession()) {
			Destination topic = session.createTopic("Hcl");
			MessageProducer sender = session.createProducer(topic);
			TextMessage hclMsg = session.createTextMessage(hcl);
			sender.send(hclMsg);
            log.debug("hcl message was sent successfully.");
		} catch (JMSException jmsEx) {
			throw new IllegalStateException("failed to send HCL: ", jmsEx);
		}
	}
}
