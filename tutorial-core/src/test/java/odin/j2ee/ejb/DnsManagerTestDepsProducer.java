package odin.j2ee.ejb;

import javax.enterprise.inject.Produces;

import odin.j2ee.api.TxScopedManagerLocator;

public class DnsManagerTestDepsProducer {
	@Produces
	public TxScopedManagerLocator createLocator() {
		return new TxScopedManagerLocator() {
			@Override
			public <T> T getManager(Class<T> iface) {
				System.out.println("mock locator invoked");
				return null;
			}
		};
	}
}
