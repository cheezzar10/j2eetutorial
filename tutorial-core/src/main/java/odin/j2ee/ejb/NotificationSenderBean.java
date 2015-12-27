package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.EJBException;
import javax.jms.JMSException;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Destination;
import javax.jms.TextMessage;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSender;

@Stateless(name = "NotificationSender")
public class NotificationSenderBean implements NotificationSender {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private JMSContext jmsCtx;
	
	@Resource(mappedName = "java:/jms/topic/notifications")
	private Destination notificationsTopic;
	
	@Override
	public void send(Integer userId, String msg) {
		try {
			log.debug("sending notification to user #{}", userId);
		
			TextMessage textMsg = jmsCtx.createTextMessage(msg);
			textMsg.setIntProperty("userId", userId);
			
			JMSProducer sender = jmsCtx.createProducer();
			sender.send(notificationsTopic, textMsg);
		
			log.debug("notification was successfully sent");
		} catch  (JMSException sendingEx) {
			throw new EJBException("notification sending failed", sendingEx);
		}
	}
}