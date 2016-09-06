package odin.j2ee.api;

import javax.ejb.Local;

@Local
public interface DnsRecordManager {
	public void removeRecord(int recId);
	
	public void removeRecordFromStore(int recId);
}
