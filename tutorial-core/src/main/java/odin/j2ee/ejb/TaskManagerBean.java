package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.stats.Stats;
import org.infinispan.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.TaskManager;
import odin.j2ee.model.TaskExecution;

@Stateless(name = "TaskManager")
public class TaskManagerBean implements TaskManager {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private Instance<JMSContext> jmsCtx;
	
	@Resource(lookup = "java:/jms/queue/tasks")
	private Destination tasksQueue;
	
	@Resource(name = "taskmgr/tasks")
	private Cache<String, TaskExecution> cache;

	@Resource
	private SessionContext sessionContext;
	
	@PostConstruct
	private void init() {
		log.debug("tasks cache started");
	}

	@PreDestroy
	private void destroy() {
		log.debug("clearing tasks cache");
		cache.clear();
		log.debug("tasks cache cleared");
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void execute(TaskExecution execution) {
		storeTaskExecutionParametersInCache(execution);

		log.debug("submitting task {} execution request", execution.getTaskName());

		ConnectionMetaData connMeta = jmsCtx.get().getMetaData();
		try {
			Enumeration<?> propNames = connMeta.getJMSXPropertyNames();
			while (propNames.hasMoreElements()) {
				log.debug("JMSX prop: {}", propNames.nextElement());
			}
		} catch (JMSException jmsEx) {
			log.error("failed to log JMSX property names");
		}
		
		JMSProducer sender = jmsCtx.get().createProducer();
		sender.send(tasksQueue, execution);

		log.debug("task {} execution request submitted", execution.getTaskName());
	}

	private void storeTaskExecutionParametersInCache(TaskExecution execution) {
		log.debug("storing task {} execution parameters in cache", execution.getTaskName());

		if (cache.get(execution.getTaskName()) == null) {
			log.debug("task {} execution parameter are not cached yet", execution.getTaskName());
		}

		boolean locked = cache.getAdvancedCache().lock(execution.getTaskName());
		log.debug("task {} execution parameters cache entry locked: {}", execution.getTaskName(), locked);

		try {
			cache.put(execution.getTaskName(), execution);
		} catch (TimeoutException timeoutEx) {
			log.error(
					"failed to store task {} execution parameters in cache due to exceeded lock timeout",
					execution.getTaskName());
			log.debug("transaction has been marked for rollback: {}", sessionContext.getRollbackOnly());
		}
		log.debug("task {} execution parameters stored in cache", execution.getTaskName());

		// sleep(20);
	}

	private void sleep(int seconds) {
		try {
			Thread.sleep(20 * 1000);
		} catch (InterruptedException intrEx) {
			log.warn("interrupted");
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public Map<String, String> getCacheStats() {
		AdvancedCache<String, TaskExecution> advancedCache = cache.getAdvancedCache();
		Stats stats = advancedCache.getStats();
		Map<String, String> rv = new HashMap<>();
		rv.put("evictions", String.valueOf(stats.getEvictions()));
		rv.put("stores", String.valueOf(stats.getStores()));
		rv.put("entries", String.valueOf(stats.getCurrentNumberOfEntries()));
		rv.put("status", advancedCache.getStatus().toString());
		return rv;
	}
}
