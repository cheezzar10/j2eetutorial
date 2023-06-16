package odin.j2ee.ejb;

import odin.j2ee.api.DnsManager;
import odin.j2ee.api.DnsRecordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;

@Stateless(name = "DnsManager")
public class DnsManagerBean implements DnsManager {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DnsRecordManager dnsRecordManager;

	@Override
	public void createDomain(String domainName) {
		log.debug("creating domain: {}", domainName);
	}

	@Override
	public void removeDomain(String domainName) {
		log.debug("removing domain: {}", domainName);

		dnsRecordManager.removeRecord(1);
	}
}
