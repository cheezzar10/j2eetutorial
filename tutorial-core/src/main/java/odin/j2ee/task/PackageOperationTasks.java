package odin.j2ee.task;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

@Singleton(name = "PackageInstallationTask")
@Startup
@Lock(LockType.READ)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PackageOperationTasks {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@PostConstruct
	private void init() {
		log.debug("package task handlers instance {} initialized", this);
		
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			LocateRegistry.createRegistry(1099);
			
			JMXConnectorServer jmxConnServer = JMXConnectorServerFactory.newJMXConnectorServer(
					new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi"), null, mBeanServer);
			jmxConnServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@PreDestroy
	private void onDestroy() {
		LoggerContext logCtx = (LoggerContext)LoggerFactory.getILoggerFactory();
		logCtx.stop();
	}
	
	@Task(name = "Install Package")
	public void install(Map<String, String> params) {
		log.debug("package installation started with parameters: {}", params);
	}
}
