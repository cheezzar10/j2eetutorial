package odin.j2ee.task;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

@Singleton(name = "PackageInstallationTask")
@Startup
@Lock(LockType.READ)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PackageOperationTasks {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Resource(lookup = "java:jboss/ee/concurrency/factory/default")
	private ManagedThreadFactory threadFactory;
	
	@PostConstruct
	public void init() {
		log.debug("package task handlers instance {} initialized", this);
		
		try {
			dumpJndiTree();
			// initJmxServer();
		} catch (Exception e) {
			log.error("startup failure: ", e);
		}
	}
	
	private void initJmxServer() {
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
	
	private void dumpJndiTree() throws Exception {
		InitialContext rootCtx = new InitialContext();
		
		Context ctx = (Context) rootCtx.lookup("java:jboss");
		dumpContext(ctx);
	}

	private void dumpContext(Context ctx) throws Exception {
		NamingEnumeration<NameClassPair> entries = ctx.list("");
		while (entries.hasMore()) {
			NameClassPair entry = entries.next();
			if ("javax.naming.Context".equals(entry.getClassName())) {
				log.debug("context: {}", entry.getName());
				dumpContext((Context)ctx.lookup(entry.getName()));
			} else {
				log.debug("{}: {}", entry.getName(), entry.getClassName());
			}
		}
	}

	@PreDestroy
	private void onDestroy() {
		LoggerContext logCtx = (LoggerContext)LoggerFactory.getILoggerFactory();
		logCtx.stop();
	}
	
	@Task(name = "Install Package")
	public void install(Map<String, String> params) throws Exception {
		Thread installTask = threadFactory.newThread(() -> {
			log.debug("package installation started with parameters: {}", params);
		});
		installTask.setName("task-install-01");
		installTask.start();
		
		Thread.sleep(100_000);
	}
	
	// @Task(name = "Update Package")
	public void update(Map<String, String> params) {
		log.debug("package update task started with parameters: {}", params);
	}
}
