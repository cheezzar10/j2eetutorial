package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationChannel;
import odin.j2ee.api.NotificationSubscription;
import odin.j2ee.api.NotificationSubscriptionRegistry;
import odin.j2ee.api.ReceiversLimitExceededException;
import odin.j2ee.api.ReceivingFailedException;
import odin.j2ee.api.SubscriptionActivationFailedException;

@Path("/notifications")
public class NotificationService {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@POST
	@Path("/subscriptions/{userId}")
	public Response subscribe(@PathParam("userId") Integer userId) {
		log.debug("subscribing user #{} for notifications", userId);
		
		try {
			return Response.ok(registry.subscribe(userId)).build();
		} catch (SubscriptionActivationFailedException activationFailedEx) {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build(); 
		}
	}
	
	@GET
	@Path("/{subscriptionId}")
	public Response receive(@PathParam("subscriptionId") String subscriptionId) {
		NotificationChannel channel = registry.getSubscription(subscriptionId);
		if (channel == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		NotificationSubscription subscription = channel.getSubscription();
		try {
			return Response.ok(subscription.receive()).build();
		} catch (ReceiversLimitExceededException tooManyReceiversEx) {
			subscription.deactivate();
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		} catch (ReceivingFailedException receivingFailedEx) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}