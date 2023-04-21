package odin.j2ee.ejb;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.TxScopedManagerLocator;

@Named("txScopedManagerLocator")
@TransactionScoped
public class TxScopedManagerLocatorBean implements TxScopedManagerLocator, Serializable {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Map<String, String> managers = Map.of(
			"odin.j2ee.api.DnsRecordManager",
			"java:module/DnsRecordManager");

	private transient final Map<String, Object> managerInstances = new HashMap<>();

	@Resource(mappedName = "java:jboss/TransactionManager")
	private transient TransactionManager transactionManager;

	@PostConstruct
	private void init() {
		log.debug("transaction scoped manager locator instance @{} created", hashCode());
	}

	@PreDestroy
	private void destroy() {
		log.debug("transaction scoped manager locator instance @{} destroyed", hashCode());
	}

	@Override
	public <T> T getManager(Class<T> iface) {
		logTransactionStatus();

		return iface.cast(managerInstances.computeIfAbsent(iface.getName(), this::createManagerInstance));
	}

	private void logTransactionStatus() {
		try {
			int transactionStatus = transactionManager.getStatus();
			log.debug("transaction status: {}", transactionStatus);
		} catch (SystemException systemException) {
			log.debug("failed to resolve transaction status");
		}
	}

	private Object createManagerInstance(String interfaceName) {
		log.debug("creating new manager instance implementing interface: {}", interfaceName);

		String jndiName = managers.get(interfaceName);
		if (jndiName == null) {
			throw new IllegalArgumentException(
					"failed to find implementation of manager interface: %s".formatted(interfaceName));
		}

		try {
			return new InitialContext().lookup(jndiName);
		} catch (NamingException namingException) {
			throw new RuntimeException("manager instance lookup failed: ", namingException);
		}
	}
}
