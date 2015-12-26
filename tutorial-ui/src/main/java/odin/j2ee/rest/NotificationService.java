package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationSubscriptionRegistry;

@Path("/notifications")
public class NotificationService {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@POST
	@Path("/subscriptions/{userId}")
	public String subscribe(@PathParam("userId") Integer userId) {
		log.debug("subscribing user #{} for notifications", userId);
		return registry.subscribe(userId);
	}
}