package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.transaction.TransactionSynchronizationRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.TxScopedManagerLocator;

@Singleton(name = "TxScopedManagerLocator")
@Lock(LockType.READ)
public class TxScopedManagerLocatorBean implements TxScopedManagerLocator {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final Map<String, Object> refs = new ConcurrentHashMap<>();
	
	@Resource
	private SessionContext ctx;
	
	@Resource
	private TransactionSynchronizationRegistry registry;
	
	private final Map<String, String> managers = new HashMap<>();
	
	@PostConstruct
	private void init() {
		managers.put("odin.j2ee.api.DnsRecordManager", "java:module/DnsRecordManager");
	}
	
	@Override
	public <T> T getManager(Class<T> iface) {
		String ifaceName = iface.getName();
		Object mgrObj = refs.get(ifaceName);
		
		if (mgrObj != null) {
			return iface.cast(mgrObj);
		}
		
		log.debug("locating TX {} scoped manager implementing interface: {}", registry.getTransactionKey(), ifaceName);
		
		String jndiName = managers.get(ifaceName);
		log.debug("performing JNDI lookup: {}", jndiName);
		
		mgrObj = ctx.lookup(jndiName);
		refs.put(ifaceName, mgrObj);
		
		return iface.cast(mgrObj);
	}
}
