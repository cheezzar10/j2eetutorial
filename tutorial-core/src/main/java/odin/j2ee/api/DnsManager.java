package odin.j2ee.api;

import javax.ejb.Remote;

@Remote
public interface DnsManager {
	public void createDomain(String domainName);
	
	public void removeDomain(String domainName);
}
