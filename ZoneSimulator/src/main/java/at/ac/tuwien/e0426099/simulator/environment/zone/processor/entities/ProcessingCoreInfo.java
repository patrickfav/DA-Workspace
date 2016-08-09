package at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities;

import java.util.Date;
import java.util.UUID;

/**
 * This will wrap the volatile informations from a core to a single unit, so
 * the scheduler can examine it thread-safe.
 *
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingCoreInfo {
	private UUID id;
	private String name;
	private double load;
	private int currentRunningTasks;
	private int maxConcurrentTasks;
	private long createdTimestamp;

	public ProcessingCoreInfo(UUID id, String name, double load, int currentRunningTasks, int maxConcurrentTasks) {
		this.id = id;
		this.name = name;
		this.load = load;
		this.currentRunningTasks = currentRunningTasks;
		this.maxConcurrentTasks = maxConcurrentTasks;
		createdTimestamp = new Date().getTime();
	}

	public Double getLoad() {
		return load;
	}

	public int getCurrentRunningTasks() {
		return currentRunningTasks;
	}

	public int getMaxConcurrentTasks() {
		return maxConcurrentTasks;
	}

	public UUID getId() {
		return id;
	}

	public long getCreatedTimestamp() {
		return createdTimestamp;
	}

	public String getName() {
		return name;
	}


	@Override
	public String toString() {
		return "ProcessingCoreInfo{" +
				"id=" + id +
				", name='" + name + '\'' +
				", load=" + load +
				", currentRunningTasks=" + currentRunningTasks +
				", maxConcurrentTasks=" + maxConcurrentTasks +
				", createdTimestamp=" + createdTimestamp +
				'}';
	}
}