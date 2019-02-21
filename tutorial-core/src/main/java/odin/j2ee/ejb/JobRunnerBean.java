package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.ejb.Schedule;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless(name = "JobRunner")
public class JobRunnerBean {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	// @Schedule(hour = "*", minute = "*", second = "*/2", persistent = false)
	private void processJobQueue() {
		log.debug("delayed jobs queue processing started using processor @{}", hashCode());
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		log.debug("delayed jobs queue processing completed using processor @{}", hashCode());
	}
}
