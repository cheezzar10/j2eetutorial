package odin.j2ee.ejb;

import java.io.File;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import odin.j2ee.api.NotificationSubscriptionRegistry;
import odin.j2ee.model.TaskExecution;
import odin.j2ee.model.TaskSupport;
import odin.j2ee.task.TaskDetector;

@RunWith(Arquillian.class)
public class NotificationSubscriptionRegistryTest {
	@EJB
	private NotificationSubscriptionRegistry registry;
	
//	@Deployment
	public static JavaArchive createEjbJar() {
		JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "core.jar")
				.addPackage(NotificationSubscriptionRegistry.class.getPackage())
				.addPackage(NotificationSubscriptionRegistryBean.class.getPackage())
				.addPackage(TaskExecution.class.getPackage())
				.addPackage(TaskDetector.class.getPackage())
				.addAsManifestResource("core-jms.xml")
				.addAsManifestResource("META-INF/ejb-jar.xml", "ejb-jar.xml");
		System.out.printf("archive content: %s%n", archive.toString(true));
		archive.as(ZipExporter.class).exportTo(new File("tutorial-core.jar"), true);
		return archive;
	}
	
	@Deployment
	public static EnterpriseArchive creatTestEar() {
		EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "test.ear");
		JavaArchive coreJar = null;
		try {
		coreJar = ShrinkWrap.create(JavaArchive.class, "core.jar")
				.addPackage(Package.getPackage("odin.j2ee.api"))
				.addPackage(Package.getPackage("odin.j2ee.ejb"))
				.addPackage(TaskSupport.class.getPackage())
				.addPackage(TaskDetector.class.getPackage())
				.addAsManifestResource("META-INF/ejb-jar.xml", "ejb-jar.xml");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(coreJar.toString(true));
		archive.addAsModule(coreJar);
		archive.addAsLibraries(Maven.resolver()
				.loadPomFromFile("pom.xml")
				.resolve("org.apache.commons:commons-collections4")
				.withoutTransitivity()
				.asFile());
		archive.addAsLibraries(Maven.resolver()
				.loadPomFromFile("pom.xml")
				.resolve("ch.qos.logback:logback-classic")
				.withTransitivity().
				asFile());
		archive.addAsManifestResource("META-INF/jboss-deployment-structure.xml", "jboss-deployment-structure.xml");
		System.out.println(archive.toString(true));
		return archive;
	}
	
	@Test
	public void testSubscribe() throws Exception {
		registry.subscribe(1);
	}
}
