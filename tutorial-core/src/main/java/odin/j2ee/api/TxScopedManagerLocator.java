package odin.j2ee.api;

import javax.ejb.Local;

@Local
public interface TxScopedManagerLocator {
	<T> T getManager(Class<T> iface);
}
