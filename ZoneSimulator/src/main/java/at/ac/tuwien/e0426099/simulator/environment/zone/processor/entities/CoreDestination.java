package at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities;

import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

import java.util.UUID;

/**
 * Wraps the subtask and the core where it needs to go
 *
 * @author PatrickF
 * @since 07.12.12
 */
public class CoreDestination {
	private UUID coreId;
	private SubTaskId subTaskId;

	public CoreDestination(UUID coreId, SubTaskId task) {
		this.coreId = coreId;
		this.subTaskId = task;
	}

	public UUID getCoreId() {
		return coreId;
	}

	public SubTaskId getSubTaskId() {
		return subTaskId;
	}
}
