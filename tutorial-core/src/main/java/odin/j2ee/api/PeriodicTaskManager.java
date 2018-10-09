package odin.j2ee.api;

public interface PeriodicTaskManager {
	void scheduleTask(String name, long interval);

	void cancelTask(String name);
}
