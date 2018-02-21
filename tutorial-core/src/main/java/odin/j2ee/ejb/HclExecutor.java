package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(name = "HclExecutor", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/topic/Hcl"),
		@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
		@ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "HclExecutor"),
		// @ActivationConfigProperty(propertyName = "clientId", propertyValue = "HclExecutor"),
		@ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
})
public class HclExecutor implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	public void onMessage(Message msg) {
		log.debug("received message");
		
		TextMessage hclMsg = (TextMessage)msg;
		
		try {
			String hcl = hclMsg.getText();
			log.debug("executing HCL: {}", hcl);
		} catch (JMSException jmsEx) {
			throw new IllegalArgumentException("HCL processing failed: ", jmsEx);
		}
	}
}
