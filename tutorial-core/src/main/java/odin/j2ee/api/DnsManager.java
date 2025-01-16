package odin.j2ee.api;

import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Remote
public interface DnsManager {
	void createDomain(String domainName);
	
	void removeDomain(String domainName);

	void noTxOp();
}
