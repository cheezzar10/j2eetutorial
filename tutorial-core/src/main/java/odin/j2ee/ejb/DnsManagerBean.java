package odin.j2ee.ejb;

import odin.j2ee.api.DnsManager;
import odin.j2ee.api.DnsRecordManager;
import odin.j2ee.api.TxScopedManagerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;

@Stateless(name = "DnsManager")
public class DnsManagerBean implements DnsManager {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private TxScopedManagerLocator locator;

	@Override
	public void createDomain(String domainName) {
		log.debug("creating domain: {}", domainName);
	}

	@Override
	public void removeDomain(String domainName) {
		DnsRecordManager dnsRecMgr = locator.getManager(DnsRecordManager.class);

		log.debug("removing domain: {}", domainName);
		dnsRecMgr.removeRecord(1);
	}
}
