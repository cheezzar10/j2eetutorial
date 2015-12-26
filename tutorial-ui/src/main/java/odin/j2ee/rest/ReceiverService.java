package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.NotificationReceiver;

@Path("/receiver")
public class ReceiverService {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private NotificationReceiver receiver;
	
	@GET
	@Path("/{userId}")
	public String receiveNotificationForUser(@PathParam("userId") Integer userId) {
		log.debug("trying to receive notification for user: {}", userId);
		// return receiver.receive(userId);
		return receiver.unsubscribe();
	}
}