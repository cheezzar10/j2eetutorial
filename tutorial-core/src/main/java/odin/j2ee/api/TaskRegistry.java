package odin.j2ee.api;

import java.lang.reflect.Method;
import java.util.Map;

public interface TaskRegistry {
	void registerTask(String name, Object target, Method method);

	void executeTask(String name, Map<String, String> params);
}
