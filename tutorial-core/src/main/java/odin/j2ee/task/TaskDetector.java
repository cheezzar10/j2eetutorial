package odin.j2ee.task;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskDetector {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@PostConstruct
	public void detect(InvocationContext invocation) {
		Object target = invocation.getTarget();
		log.debug("performing task handlers search in {}", target);
		
		Class<?> targetClass = target.getClass();
		for (Method method : targetClass.getDeclaredMethods()) {
			log.debug("method: {}", method.getName());
			Task taskAnnot = method.getAnnotation(Task.class);
			if (taskAnnot != null) {
				log.debug("registering method {} of {} as task {} handler", method.getName(), target, taskAnnot.value());
			}
		}
		
		try {
			invocation.proceed();
		} catch (Exception invEx) {
			throw new EJBException(invEx);
		}
	}
}
