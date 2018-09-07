package odin.j2ee.rest;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/rs")
public class Application extends javax.ws.rs.core.Application {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	public Set<Class<?>> getClasses() {
		return Collections.emptySet();
	}
	
	public Set<Object> getSingletons() {
		log.debug("get singletons factory method");
		
		return Collections.emptySet();
	}
}