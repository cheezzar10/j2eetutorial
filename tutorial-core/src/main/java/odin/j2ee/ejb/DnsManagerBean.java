package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DnsManager;
import odin.j2ee.api.DnsRecordManager;
import odin.j2ee.api.TxScopedManagerLocator;

@Stateless(name = "DnsManager")
public class DnsManagerBean implements DnsManager {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private TxScopedManagerLocator locator;

	@Override
	public void createDomain(String domainName) {
		log.debug("creating domain: {}", domainName);
	}

	@Override
	public void removeDomain(String domainName) {
		log.debug("removing domain: {}", domainName);
		DnsRecordManager dnsRecMgr = locator.getManager(DnsRecordManager.class);
		dnsRecMgr.removeRecord(1);
	}

	@Override
	public void recordRemoved(int recId) {
		log.debug("marking record {} as removed", recId);
		DnsRecordManager dnsRecMgr = locator.getManager(DnsRecordManager.class);
		dnsRecMgr.removeRecordFromStore(recId);
	}
}
