package odin.j2ee.ejb;

import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import odin.j2ee.api.DnsManager;
import odin.j2ee.api.TxScopedManagerLocator;
import odin.j2ee.task.Task;
import odin.j2ee.task.TaskDetector;

@RunWith(Arquillian.class)
public class DnsManagerTest {
	@EJB
	private DnsManager dnsMgr;
	
	@Deployment
	public static EnterpriseArchive creatTestEar() {
		EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "test.ear");
		
		JavaArchive coreJar = ShrinkWrap.create(JavaArchive.class, "core.jar")
				.addClass(DnsManagerBean.class)
				.addClass(DnsManager.class)
				.addClass(TxScopedManagerLocator.class)
				.addClass(TaskDetector.class)
				.addClass(Task.class)
				.addClass(MethodHandles.lookup().lookupClass())
				// .addClass(DnsManagerTestDepsProducer.class)
				.addAsManifestResource("META-INF/ejb-jar.xml", "ejb-jar.xml")
				.addAsManifestResource("META-INF/beans.xml", "beans.xml");
		
		archive.addAsModule(coreJar);
		
		archive.addAsLibraries(Maven.resolver()
				.loadPomFromFile("pom.xml")
				.resolve("ch.qos.logback:logback-classic")
				.withTransitivity().
				asFile());
		
		archive.addAsManifestResource("META-INF/jboss-deployment-structure.xml", "jboss-deployment-structure.xml");
		
		return archive;
	}
	
	@Test
	public void testRemoveDomain() {
		dnsMgr.removeDomain("bmwklub.de");
	}
}
