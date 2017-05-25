package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DnsManager;
import odin.j2ee.api.DnsRecordManager;
import odin.j2ee.api.TxScopedManagerLocator;

@Stateless(name = "DnsManager")
@TransactionManagement(TransactionManagementType.BEAN)
public class DnsManagerBean implements DnsManager {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private Instance<TxScopedManagerLocator> locator;
	
	@Resource
	private SessionContext ctx;

	@Override
	public void createDomain(String domainName) {
		log.debug("creating domain: {}", domainName);
	}

	@Override
	public void removeDomain(String domainName) {
		UserTransaction tx = ctx.getUserTransaction();
		try {
			tx.begin();
			log.debug("removing domain: {}", domainName);
			DnsRecordManager dnsRecMgr = locator.get().getManager(DnsRecordManager.class);
			dnsRecMgr.removeRecord(1);
			tx.commit();
			log.debug("transaction commited");
		} catch (SystemException se) {
			throw new EJBException("TX system: ", se);
		} catch (NotSupportedException nse) {
			throw new EJBException("TX not supported: ", nse);
		} catch (RollbackException re) {
			throw new EJBException("TX rollback: ", re);
		} catch (HeuristicMixedException hme) {
			throw new EJBException("TX heuristic mixed: ", hme);
		} catch (HeuristicRollbackException hre) {
			throw new EJBException("TX heuristic rollback: ", hre);
		}
	}
}
