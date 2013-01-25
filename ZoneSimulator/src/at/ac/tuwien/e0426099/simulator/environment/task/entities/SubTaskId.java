package at.ac.tuwien.e0426099.simulator.environment.task.entities;

import java.util.UUID;

/**
 * Wraps the subtask uuid and its parent id
 *
 * There is a special value parent id uuid when no parent has been specified
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SubTaskId subTaskId1 = (SubTaskId) o;

		if (parentTaskId != null ? !parentTaskId.equals(subTaskId1.parentTaskId) : subTaskId1.parentTaskId != null)
			return false;
		if (subTaskId != null ? !subTaskId.equals(subTaskId1.subTaskId) : subTaskId1.subTaskId != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = parentTaskId != null ? parentTaskId.hashCode() : 0;
		result = 31 * result + (subTaskId != null ? subTaskId.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "SubTaskId(P:"+ parentTaskId.toString().substring(0,5) +"/ST:" + subTaskId.toString().substring(0,5) +")";
	}
}
