package odin.j2ee.clnt;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JbossLogbackCtl {
	public static void main(String[] args) throws Exception {
		JMXServiceURL jmxUrl = new JMXServiceURL("service:jmx:remote+http://10.27.75.220:9990");
		JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxUrl, null);
		MBeanServerConnection jmxConn = jmxConnector.getMBeanServerConnection();
		
		if (Math.abs(-1) < 0) {
			printMBeansCount(jmxConn);
			printLogbackJmxConfigurators(jmxConn);
		}
		
		setLoggerLevelInContext(jmxConn, "tutorial-core", "odin.j2ee.ejb.DnsRecordManagerBean", "DEBUG");
		printLoggersInContext(jmxConn, "tutorial-core");
		
		jmxConnector.close();
	}
	
	private static void setLoggerLevelInContext(MBeanServerConnection jmxConn, String loggerContext, String logger, String level) throws Exception {
		ObjectName loggerConf = new ObjectName("ch.qos.logback.classic:Name=" + loggerContext + ",Type=ch.qos.logback.classic.jmx.JMXConfigurator");
		jmxConn.invoke(loggerConf, "setLoggerLevel", new Object[] { logger, level }, new String[] { String.class.getName(), String.class.getName() });
	}

	private static void printLoggersInContext(MBeanServerConnection jmxConn, String loggerContext) throws Exception {
		String loggerConfName = "ch.qos.logback.classic:Name=" + loggerContext + ",Type=ch.qos.logback.classic.jmx.JMXConfigurator";
		Object loggers = jmxConn.getAttribute(new ObjectName(loggerConfName), "LoggerList");
		for (Object logger : ((List<?>)loggers)) {
			Object level = jmxConn.invoke(new ObjectName(loggerConfName), "getLoggerLevel", new Object[] { logger }, new String[] { String.class.getName() });
			System.out.println(level + " : " + logger);
		}
	}

	private static void printMBeansCount(MBeanServerConnection jmxConn) throws Exception {
		int mbeanCount = jmxConn.getMBeanCount();
		System.out.printf("MBean count = %d%n", mbeanCount);
	}
	
	private static void printLogbackJmxConfigurators(MBeanServerConnection jmxConn) throws Exception {
		String query = "ch.qos.logback.classic:Name=*,Type=ch.qos.logback.classic.jmx.JMXConfigurator";
		for (ObjectInstance mbeanInstance : jmxConn.queryMBeans(new ObjectName(query), null)) {
			MBeanInfo mbeanInfo = jmxConn.getMBeanInfo(mbeanInstance.getObjectName());
			
			System.out.printf("---- %s attributes ----%n", mbeanInstance.getObjectName());
			for (MBeanAttributeInfo attrInfo : mbeanInfo.getAttributes()) {
				try {
					System.out.printf("%s = %s%n", attrInfo.getName(), getObjectInstanceAttrValue(jmxConn, mbeanInstance, attrInfo.getName()));
				} catch (JMRuntimeException getAttrEx) {
					System.out.printf("%s = not supported%n", attrInfo.getName());
				}
			}
			
			System.out.printf("---- %s operations ----%n", mbeanInstance.getObjectName());
			for (MBeanOperationInfo opInfo : mbeanInfo.getOperations()) {
				System.out.printf("%s %s(%s)%n", opInfo.getReturnType(), opInfo.getName(), getOperationSignature(opInfo.getSignature()));
			}
		}
	}
	
	private static Object getObjectInstanceAttrValue(MBeanServerConnection jmxConn, ObjectInstance instance, String attrName) throws IOException, JMException {
		Object attrValue = jmxConn.getAttribute(instance.getObjectName(), attrName);
		if (attrValue instanceof Object[]) {
			return Arrays.toString((Object[])attrValue);
		} else if (attrValue instanceof CompositeData) {
			CompositeData compositeData = (CompositeData)attrValue;
			CompositeType compositeType = compositeData.getCompositeType();
			List<String> dataKeyValues = new LinkedList<>();
			for (String dataKey : compositeType.keySet()) {
				dataKeyValues.add(String.format("%s = %s", dataKey, compositeData.get(dataKey)));
			}
			return dataKeyValues.stream().collect(Collectors.joining(", ", "{ ", " }"));
		} else {
			return attrValue;
		}
	}
	
	private static Object getOperationSignature(MBeanParameterInfo[] operationSignature) {
		return Stream.of(operationSignature).map(opSign -> opSign.getType() + " " + opSign.getName()).collect(Collectors.joining(", "));
	}
}
