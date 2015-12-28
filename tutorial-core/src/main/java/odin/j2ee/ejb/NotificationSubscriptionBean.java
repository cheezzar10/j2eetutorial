package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;

@Stateful(name = "NotificationSubscription")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class NotificationSubscriptionBean implements NotificationSubscription {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	private String id;
	
	private Integer userId;
	
	@PostConstruct
	private void init() {
		log.debug("new notification subscription instance @{} initialized", hashCode());
	}
	
	@Override
	public String activate(Integer userId) {
		log.debug("activating user #{} notification subscription", userId);
		id = String.valueOf(System.currentTimeMillis());
		this.userId = userId;
		log.debug("user {} notification subscription {} activated", userId, id);
		return id;
	}
	
	@Override
	public Integer getUserId() {
		return userId;
	}
	
	@Override
	@Remove
	public void deactivate() {
		log.debug("deactivating notification subscription {}", id);
		registry.removeSubscription(id);
	}
}
