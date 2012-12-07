package at.ac.tuwien.e0426099.simulator.environment.processor.entities;

import at.ac.tuwien.e0426099.simulator.environment.task.IRunnableTask;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class CoreDestination {
	private UUID coreId;
	private IRunnableTask task;

	public CoreDestination(UUID coreId, IRunnableTask task) {
		this.coreId = coreId;
		this.task = task;
	}

	public UUID getCoreId() {
		return coreId;
	}

	public IRunnableTask getTask() {
		return task;
	}
}
