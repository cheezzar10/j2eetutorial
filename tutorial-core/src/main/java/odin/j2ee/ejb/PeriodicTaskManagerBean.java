package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.PeriodicTaskManager;

@Singleton(name = "PeriodicTaskManager")
@Lock(LockType.READ)
public class PeriodicTaskManagerBean implements PeriodicTaskManager {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Resource
	private TimerService tmrSvc;
	
	private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>(); 
	
	@Override
	public void scheduleTask(String name, long interval) {
		log.debug("scheduling periodic task with name: {} and interval: {}", name, interval);

		if (interval <= 0) {
			throw new IllegalArgumentException("interval should be positive value");
		}
		
		TimerConfig tmrCnf = new TimerConfig(name, false);
		Timer tmr = tmrSvc.createIntervalTimer(0, interval, tmrCnf);
		timers.put(name, tmr);
		
		log.debug("periodic task: {} scheduled", name);
	}
	
	@Timeout
	private void runTask(Timer tmr) {
		String name = (String) tmr.getInfo();
		log.debug("periodic task {} started", name);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException intrEx) {
			Thread.currentThread().interrupt();
			log.debug("periodic task {} processing interrupted", name);
			return;
		}
		
		log.debug("periodic task {} completed", name);
	}

	@Override
	public void cancelTask(String name) {
		log.debug("cancelling periodic task with name: {}", name);
		
		Timer tmr = timers.remove(name);
		tmr.cancel();
		
		try {
			Thread.sleep(15_000);
		} catch (InterruptedException intrEx) {
			Thread.currentThread().interrupt();
			log.debug("periodic task {} cancellation interrupted", name);
			return;
		}
		
		log.debug("cancelled periodic task with name: {}", name);
	}
}
