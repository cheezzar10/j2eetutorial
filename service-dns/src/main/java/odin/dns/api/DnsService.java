package odin.dns.api;

import javax.ejb.Remote;

@Remote
public interface DnsService {
	public void onRemoveRecord(String recordType, String recordData);
}
