package odin.j2ee.ejb;

import odin.j2ee.api.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class RequestContextBean implements RequestContext {
	private static final Logger log = LoggerFactory.getLogger(RequestContextBean.class);

	private String instanceId;

	public RequestContextBean() {
		instanceId = Integer.toHexString(hashCode());

		log.debug("request context instance created");
	}

	@Override
	public String getInstanceId() {
		return instanceId;
	}
}
