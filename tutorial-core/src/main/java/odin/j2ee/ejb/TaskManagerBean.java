package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.manager.CacheContainer;
import org.infinispan.stats.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.TaskManager;
import odin.j2ee.model.TaskExecution;

@Stateless(name = "TaskManager")
@Resources({
	@Resource(name = "cache-container/taskmgr", mappedName = "java:jboss/infinispan/container/taskmgr"),
	@Resource(name = "cache/tasks", type = Configuration.class, lookup = "java:jboss/infinispan/configuration/taskmgr/tasks"),
})
public class TaskManagerBean implements TaskManager {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Resource(mappedName = "java:jboss/DefaultJMSConnectionFactory")
	private ConnectionFactory connFactory;
	
	@Resource(lookup = "java:/jms/queue/tasks")
	private Destination tasksQueue;
	
	@Resource(name = "cache-container/taskmgr")
	private CacheContainer cacheContainer;

	private Cache<String, TaskExecution> cache;
	
	@PostConstruct
	private void init() {
		cache = cacheContainer.getCache("tasks");
		log.debug("tasks cache started");
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void execute(TaskExecution execution) {
		log.debug("task execution parameters stored in cache");
		// cache.put(execution.getTaskName() + ":" + System.currentTimeMillis(), execution);
		log.debug("submitting task {} execution request", execution.getTaskName());
		
		try (Connection conn = connFactory.createConnection(); Session session = conn.createSession(); MessageProducer sender = session.createProducer(tasksQueue)) {
			ConnectionMetaData connMeta = conn.getMetaData();
			
			Enumeration<?> propNames = connMeta.getJMSXPropertyNames();
			while (propNames.hasMoreElements()) {
				log.debug("JMSX prop: {}", propNames.nextElement());
			}
			
			ObjectMessage execMsg = session.createObjectMessage(execution);
			execMsg.setStringProperty("taskName", execution.getTaskName());
			
			sender.send(execMsg);
		} catch (JMSException jmsEx) {
			log.error("failed to log JMSX property names");
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
