package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import odin.j2ee.api.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.HclSender;
import odin.j2ee.api.TaskManager;
import odin.j2ee.model.TaskExecution;

@Path("/tasks")
@Singleton
public class TaskResource {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	public TaskResource() {
		log.debug("new TaskResource instance created  @{}", this);
	}
	
	@EJB
	private TaskManager taskMgr;
	
	@EJB
	private HclSender hclSender;
	
	@Context
	private Providers providers;

	@Inject
	private RequestContext requestContext;
	
	@POST
	@Path("/hcl")
	public void sendHcl(String hcl) {
		hclSender.sendHcl(hcl);
	}
	
	@POST
	@Consumes("application/json")
	public void execute(TaskExecution execution) {
		log.debug("received execution request for task: {} with parameters: {}", execution.getTaskName(), execution.getTaskParams());
		
		Class<TaskExecution> taskExecClass = TaskExecution.class;
		MessageBodyWriter<TaskExecution> msgBodyWrtr = providers.getMessageBodyWriter(taskExecClass, null, null, MediaType.APPLICATION_JSON_TYPE);
		log.debug("message body writer: {}", msgBodyWrtr);
		
		log.debug("sending task execution request");
		taskMgr.execute(execution);

		log.debug("request context: {}", requestContext.getInstanceId());
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> getcCacheStats() {
		return taskMgr.getCacheStats();
	}

	@GET
	@Path("/token")
	public String getToken() {
		Client client = ClientBuilder.newClient();
		Configuration clientConfiguration = client.getConfiguration();
		for (Class<?> registeredCompenentClass : clientConfiguration.getClasses()) {
			// log.debug("registered component class: {}", registeredCompenentClass.getName());
		}

		WebTarget createTokenTarget =
				client.target("https://login.microsoftonline.com/8a225ab7-7c77-4046-b189-cfb89f8c5779/oauth2/token");

		Form createTokenParams = getCreateTokenParams();

		Response createTokenResponse = createTokenTarget.request()
				.post(Entity.form(createTokenParams));

		return createTokenResponse.readEntity(String.class);
	}

	private Form getCreateTokenParams() {
		Form form = new Form();

		form.param("resource", "https://api.partnercenter.microsoft.com");
		form.param("client_id", "70591bea-0c7e-45dc-adea-cc339a5fcd01");
		form.param("client_secret", "EHU7Q~wXtQvlAADvBhAa3O4R2lQnfHsOxwOM6");
		form.param("grant_type", "refresh_token");
		form.param("refresh_token", "0.ARcAt1oiind8RkCxic-4n4xXeeobWXB-DNxFrerMM5pfzQEXALk.AgABAAAAAAD--DLA3VO7QrddgJg7WevrAgDs_wQA9P_IZiwdc_MAPvLld7nfroAK0ZH25B8bgjOQbcTxwm6DML_C_FiN9UzNhQs60-U_Zqaxzbl6XgPPQXS_CsA3LeVLHR8xEQePtwn1EWijuVSwzm3ZhiXnKvUw1vZGKcTp5baf9LZRmQm1FqrnqX9dn6Lg4tVyHo1fjufxqBO8_0uo2QH95o4XGRGDWNt-vxC8e4C_SYqSH6y-oZ5bsZifN6BNgXmCxUkxXTDbsqCwGNKtvamG6OoqwMHgSItt6uz8Ym05-xNfN-_LrLkTk6Fuv6nSHLEaSAj2U06pNbrxH9gp5B-LrNh5zoB0CFPdbutDxO6NUTHNT6SoR7-Qy03onln1qHBvykIZ8d6zsRqq7uH2MyiWcii8iO6ScAFO6oAW5iS_dUrNlCWGXMgku6fqdhHuhPaph0yamY3G8GJsMLZ7x-11QPnhzLqGyYAgXFB-0kWiWfu6v-EUuqkeBZhWhkmg6aJRRzNCxbpK5O5MTjX1EuiYAtI-zTb-f0dhyP-jnBocOnEstHDDzbmvLvHzDrDUj4nYJmlpe4ZJ_d-AMB5GqvF9SO-0zuPz6FzXRpgqQxtpH51mHqlIx8KbI5GRvQDkQLAW3NHrEy3SsBBsHuDDt1gQS4CzDHvB5jvV6XoT9NisddhrMGuyp8AnoN2L-_k2KW92Dp_3QcLpqmMX2qQ7ovVaqe1RVwzt0Bqxf0BZ_mvUvLFfd_egaKa-gptLqwZoC1o1FQrsMUWrnH7nysoTwW65pN28RpVcqgcphRzce1xqxmAkV8ymSUr2DabvQlXoBGqOKznCgIf2tkNLm49VUHCOjZx7aYo_xu1uPJlXw06-2eBuxqj_Qk3VSVoSWlYZYikpabH-QzH2wpeRo88xDZkhmW0MOxj-srhgysT2uC4mkHrVSnsEOd-VToxMFBB9XNGGi1BHtHtbtKt7l9uPl1sb9dwVBJRc-m8ppryGVRjYRNUIyVRrhiaeHZIwNu-JnLVSuUXRKPN8FbKp8OonbmoTjDUDWx0g5TJ8hdi1WN0MJGI-rpuOnuIpKWvCY5pP7B-xIYi4d--13ZLGX2yoThU__gdqaBHp3FTmPa0");
		form.param("scope", "openid");

		return form;
	}
}
