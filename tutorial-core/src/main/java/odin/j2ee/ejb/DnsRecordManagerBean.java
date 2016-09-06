package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.dns.api.DnsService;
import odin.j2ee.api.DnsRecordManager;

@Stateful(name = "DnsRecordManager")
public class DnsRecordManagerBean implements DnsRecordManager, SessionSynchronization {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final Set<Integer> removedRecIds = new HashSet<>();

	@Override
	public void removeRecord(int recId) {
		log.debug("DNSRECMGR @{} marking DNS record #{} as removed", hashCode(), recId);
		removedRecIds.add(recId);
		
		try {
			Hashtable<String, String> props = new Hashtable<>();
			props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			InitialContext context = new InitialContext(props);
			
			DnsService service = (DnsService)context.lookup("ejb:service-dns//DnsService");
			service.onRemoveRecord("A", "foo.bar");
		} catch (NamingException ne) {
			throw new IllegalStateException("dns service call failed: ", ne);
		}
	}

	@Override
	public void afterBegin() throws EJBException, RemoteException {
		// Nothing to do
	}

	@Override
	public void afterCompletion(boolean committed) throws EJBException, RemoteException {
		// Nothing to do
	}

	@Override
	public void beforeCompletion() throws EJBException, RemoteException {
		log.debug("DNSRECMGR @{} performing DNS records deletion", hashCode());
		log.debug("DNSRECMGR @{} {} DNS records deleted", hashCode(), removedRecIds);
	}

	@Override
	public void removeRecordFromStore(int recId) {
		log.debug("DNSRECMGR @{} removing DNS record #{} from DB", hashCode(), recId);
		// TODO remove record from DB
	}
}
