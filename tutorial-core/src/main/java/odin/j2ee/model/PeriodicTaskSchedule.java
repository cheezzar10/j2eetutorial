package odin.j2ee.model;

public class PeriodicTaskSchedule {
	private String name;
	private long interval;
	
	public PeriodicTaskSchedule() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	public String toString() {
		return "PeriodicTaskSchedule: { name: " + name + ", interval: " + interval + " }";
	}
}
