package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DnsRecordManager;

@Stateful(name = "DnsRecordManager")
public class DnsRecordManagerBean implements DnsRecordManager, SessionSynchronization {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final Set<Integer> removedRecIds = new HashSet<>();

	@Override
	public void removeRecord(int recId) {
		log.debug("DNSRECMGR @{} marking DNS record #{} as removed", hashCode(), recId);
		removedRecIds.add(recId);
	}

	@Override
	public void afterBegin() throws EJBException, RemoteException {
		// Nothing to do
	}

	@Override
	public void afterCompletion(boolean committed) throws EJBException, RemoteException {
		log.debug("DNSRECMGR @{} tx {}", hashCode(), committed ? "committed" : "rolledback");
	}

	@Override
	public void beforeCompletion() throws EJBException, RemoteException {
		log.debug("DNSRECMGR @{} performing DNS records deletion", hashCode());
		for (Integer recId : removedRecIds) {
			log.debug("DNSRECMGR @{} removing DNS record #{}", hashCode(), recId);
		}
		log.debug("DNSRECMGR @{} {} DNS records deleted", hashCode(), removedRecIds);
	}
}
