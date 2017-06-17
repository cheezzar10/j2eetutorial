package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.TaskRegistry;

@Singleton(name = "TaskRegistry")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TaskRegistryBean implements TaskRegistry {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final ConcurrentMap<String, Pair<Object, Method>> tasks = new ConcurrentHashMap<>();

	@Override
	public void registerTask(String name, Object target, Method method) {
		tasks.put(name, new ImmutablePair<>(target, method));
		log.debug("task {} registered", name);
	}

	@Override
	public boolean executeTask(String name, Map<String, String> params) {
		Pair<Object, Method> handler = tasks.get(name);
		if (handler == null) {
			log.debug("task {} not registered", name);
			return false;
		}
		
		log.debug("executing task {}", name);
		
		try {
			handler.getRight().invoke(handler.getLeft(), params);
			return true;
		} catch (Exception ex) {
			log.error("task execution failed: ", ex);
			return false;
		}
	}
}
