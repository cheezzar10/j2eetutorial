package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

@Path("/users")
public class ProxyResource {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final Client client;
	
	public ProxyResource() {
		log.debug("new ProxyResource instance created @{}", this);
		ClientBuilder clientBuilder = ClientBuilder.newBuilder();
		((ResteasyClientBuilder)clientBuilder).connectionPoolSize(8);
		client = clientBuilder.build();
	}
	
	@POST
	public Response create(String data) {
		log.debug("forwarding user creation to external endpoint");
		Response response = client.target("http://localhost:10080/webapp/rs/users")
				.request()
				.post(Entity.json(data));
		log.debug("response received");
		return response;
	}
}
