package odin.j2ee.task;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
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
		
		performAppXmlScan();
		processRunningModules();
	}
	
	private void processRunningModules() {
		log.debug("scanning module loaders");
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			Set<ObjectInstance> serviceModuleLoaders = mbeanServer.queryMBeans(new ObjectName("jboss.modules:type=ModuleLoader,name=ServiceModuleLoader*"), null);
			for (ObjectInstance serviceModuleLoader : serviceModuleLoaders) {
				log.debug("scanning module loader: {}", serviceModuleLoader.getObjectName());
				String[] loadedModules = (String[])mbeanServer.invoke(serviceModuleLoader.getObjectName(), "queryLoadedModuleNames", null, null);
				log.debug("loaded modules: {}", Arrays.toString(loadedModules));
			}
		} catch (JMException jmxEx) {
			log.error("failed to get list of loaded modules: ", jmxEx);
		}
	}

	private void performAppXmlScan() {
		log.debug("performing application.xml scanning");
		
		ClassLoader thisClassLoader = this.getClass().getClassLoader();
		log.debug("this class loader: {}", thisClassLoader == null ? "null" : thisClassLoader.toString());
		
		ClassLoader thisClassParentLoader = thisClassLoader.getParent();
		log.debug("this class parent loader: {}", thisClassParentLoader == null ? "null" : thisClassParentLoader.toString());
		
		ClassLoader threadContextLoader = Thread.currentThread().getContextClassLoader();
		log.debug("thread context loader: {}", threadContextLoader == null ? "null" : threadContextLoader.toString());
		
		ClassLoader threadContextParentLoader = threadContextLoader.getParent();
		log.debug("thread context parent loader: {}", threadContextParentLoader == null ? "null" : threadContextParentLoader.toString());
		
		ClassLoader libLoader = Logger.class.getClassLoader();
		log.debug("lib loader: {}", libLoader == null ? "null" : libLoader.toString());
		
		URL appXmlUrl = libLoader.getResource("META-INF/application.xml");
		log.debug("application.xml url: {}", appXmlUrl == null ? "null" : appXmlUrl.toString());
		
		ClassLoader libParentLoader = libLoader.getParent();
		log.debug("lib parent loader: {}", libParentLoader == null ? "null" : libParentLoader.toString());
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
