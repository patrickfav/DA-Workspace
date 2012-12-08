package at.ac.tuwien.e0426099.simulator.environment.task.entities;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class SubTaskId {
	private UUID parentTaskId;
	private UUID subTaskId;

	public SubTaskId(UUID parentTaskId, UUID subTaskId) {
		this.parentTaskId = parentTaskId;
		this.subTaskId = subTaskId;
	}

	public UUID getParentTaskId() {
		return parentTaskId;
	}

	public UUID getSubTaskId() {
		return subTaskId;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SubTaskId && ((SubTaskId) obj).getParentTaskId().equals(parentTaskId) && ((SubTaskId) obj).getSubTaskId().equals(subTaskId); //yea bitch, thats some one line code!
	}
}
