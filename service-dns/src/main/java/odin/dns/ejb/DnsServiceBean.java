package odin.dns.ejb;

import java.util.Hashtable;

import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import odin.j2ee.api.DnsManager;
import odin.dns.api.DnsService;

@Stateless(name = "DnsService")
public class DnsServiceBean implements DnsService {
	@Override
	public void onRemoveRecord(String recordType, String recordData) {
		try {
			Hashtable<String, String> props = new Hashtable<>();
			props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			InitialContext context = new InitialContext(props);
			
			DnsManager dnsMgr = (DnsManager)context.lookup("ejb:tutorial-app/tutorial-core//DnsManager!" + DnsManager.class.getName());
			dnsMgr.recordRemoved(1);
		} catch (NamingException ne) {
			throw new IllegalStateException("dns manager call failed: ", ne);
		}
	}
}
