package odin.j2ee.ejb;

import odin.j2ee.api.DnsManager;
import odin.j2ee.api.DnsRecordManager;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

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
	@TransactionTimeout(value = 60, unit = TimeUnit.MINUTES)
	public void removeDomain(String domainName) {
		log.debug("removing domain: {}", domainName);

		dnsRecordManager.removeRecord(1);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void noTxOp() {
		dnsRecordManager.removeRecord(0);
	}
}
