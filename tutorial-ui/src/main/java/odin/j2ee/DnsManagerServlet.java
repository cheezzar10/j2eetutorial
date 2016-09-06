package odin.j2ee;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odin.j2ee.api.DnsManager;

@WebServlet("/dns")
public class DnsManagerServlet extends HttpServlet {
	private static final long serialVersionUID = 4946832175599908685L;

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private DnsManager dnsMgr;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.debug("interacting with DNS manager");
		dnsMgr.removeDomain("foo.bar");
	}
}
