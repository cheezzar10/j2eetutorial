package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DnsRecordManager;

@Stateful(name = "DnsRecordManager")
@AccessTimeout(0)
@StatefulTimeout(0)
public class DnsRecordManagerBean implements DnsRecordManager, SessionSynchronization {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final Set<Integer> removedRecIds = new HashSet<>();
	
	private static final AtomicInteger instanceCount = new AtomicInteger();
	
	@Resource
	private SessionContext ctx;
	
	public DnsRecordManagerBean() {
		log.debug("DnsRecordManager bean instance @{} created. {} instances are in total", 
				hashCode(), instanceCount.incrementAndGet());
	}

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
		log.trace("DNSRECMGR @{} tx {}", hashCode(), committed ? "committed" : "rolledback");
		ctx.getBusinessObject(DnsRecordManager.class).destroy();
	}

	@Override
	public void beforeCompletion() throws EJBException, RemoteException {
		log.trace("DNSRECMGR @{} performing DNS records deletion", hashCode());
		for (Integer recId : removedRecIds) {
			log.debug("DNSRECMGR @{} removing DNS record #{}", hashCode(), recId);
		}
		log.trace("DNSRECMGR @{} {} DNS records deleted", hashCode(), removedRecIds);
	}
	
	@PreDestroy
	private void preDestroy() {
		log.debug("DnsRecordManager bean instance @{} removed. {} instance are in total",
				hashCode(), instanceCount.decrementAndGet());
	}

	@Remove
	public void destroy() {
		log.debug("received request to destroy DnsRecordManager bean instance @{}", hashCode());
	}
}
