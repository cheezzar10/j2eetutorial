package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationReceiver;

@Stateless
public class NotificationReceiverBean implements NotificationReceiver {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private JMSContext jmsCtx;
	
	@Resource(mappedName = "java:/jms/topic/notifications")
	private Topic topic;
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public String receive(Integer userId) {
		log.debug("trying to receive notification for user: {}", userId);
		
		try {
			JMSConsumer consumer = jmsCtx.createDurableConsumer(topic, "receiver");
			// MessageConsumer consumer = session.createConsumer(topic, String.format("userId = %d", userId));
			log.debug("message consumer created - receiving");
			TextMessage msg = (TextMessage)consumer.receive(10000);
			if (msg != null) {
				log.debug("notification {} was successfully received", msg.getText());
				return msg.getText();
			} else {
				return "no message";
			}
		}
		catch (JMSException jmsEx) {
			log.error("failed to receive: ", jmsEx);
			throw new EJBException(jmsEx);
		}
	}

	@Override
	public String unsubscribe() {
		log.debug("removing subscription");
		jmsCtx.unsubscribe("1484424185881");
		return "";
	}
}