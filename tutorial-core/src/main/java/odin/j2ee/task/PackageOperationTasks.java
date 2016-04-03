package odin.j2ee.task;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton(name = "PackageInstallationTask")
@Startup
@Lock(LockType.READ)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PackageOperationTasks {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@PostConstruct
	private void init() {
		log.debug("package task handlers instance {} initialized", this);
	}
	
	@Task(name = "Install Package")
	public void install(Map<String, String> params) {
		log.debug("package installation started with parameters: {}", params);
	}
}
