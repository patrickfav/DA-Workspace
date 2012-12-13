package at.ac.tuwien.e0426099.simulator.environment.task.entities;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class SubTaskId {
	private static final UUID HAS_NO_PARENT_UUID = UUID.fromString("97c3f5c5-928a-4eb3-8aed-0fa1990c0d92");

	private UUID parentTaskId;
	private UUID subTaskId;

	public SubTaskId() {
		this.parentTaskId = HAS_NO_PARENT_UUID;
		this.subTaskId = UUID.randomUUID();
	}

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

	public void upadteParentId(UUID parentTaskId) {
		this.parentTaskId = parentTaskId;
	}

	public boolean hasNoParent() {
		return parentTaskId.equals(HAS_NO_PARENT_UUID);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SubTaskId && ((SubTaskId) obj).getParentTaskId().equals(parentTaskId) && ((SubTaskId) obj).getSubTaskId().equals(subTaskId); //yea bitch, thats some one line code!
	}

	@Override
	public String toString() {
		return "SubTaskId{" +
				"parentTaskId=" + parentTaskId.toString().substring(0,5) +
				"..., subTaskId=" + subTaskId.toString().substring(0,5) +
				"...}";
	}
}
