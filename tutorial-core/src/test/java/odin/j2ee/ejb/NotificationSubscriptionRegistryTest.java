package odin.j2ee.ejb;

import java.io.File;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import odin.j2ee.api.NotificationSubscriptionRegistry;
import odin.j2ee.model.TaskExecution;
import odin.j2ee.task.TaskDetector;

@RunWith(Arquillian.class)
public class NotificationSubscriptionRegistryTest {
	@EJB
	private NotificationSubscriptionRegistry registry;
	
	@Deployment
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
	
//	public void setup() throws Exception {
//		System.out.println("performing wfly pre-test setup");
//		CommandContext cmdCtx = CommandContextFactory.getInstance().newCommandContext();
//		cmdCtx.connectController();
//		cmdCtx.handle("jms-topic add --topic-address=notifications --entries=java:/jms/topic/notifications");
//		cmdCtx.handle("jms-queue add --queue-address=notifications --entries=java:/jms/queue/notifications --durable=false");
//		cmdCtx.handle("jms-queue add --queue-address=tasks --entries=java:/jms/queue/tasks --durable=false");
//		cmdCtx.terminateSession();
//	}
	
	@Test
	public void testSubscribe() throws Exception {
		registry.subscribe(1);
	}
}
