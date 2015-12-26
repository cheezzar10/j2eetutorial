package odin.j2ee.rest;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

// TODO rename to something more meaningful
@ApplicationPath("/rs")
public class Application extends javax.ws.rs.core.Application {
	public Set<Class<?>> getClasses() {
		return Collections.emptySet();
	}
	
	public Set<Object> getSingletons() {
		return Collections.emptySet();
	}
}