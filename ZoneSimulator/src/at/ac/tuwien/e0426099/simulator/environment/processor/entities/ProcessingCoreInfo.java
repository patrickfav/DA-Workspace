package at.ac.tuwien.e0426099.simulator.environment.processor.entities;

import java.util.Date;
import java.util.UUID;

public class ProcessingCoreInfo {
		private UUID id;
		private double load;
		private int currentRunningTasks;
		private int maxConcurrentTasks;
		private long createdTimestamp;

		public ProcessingCoreInfo(UUID id, double load, int currentRunningTasks, int maxConcurrentTasks) {
			this.id = id;
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
}