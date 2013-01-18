package at.ac.tuwien.e0426099.simulator.environment.processor.listener;

import at.ac.tuwien.e0426099.simulator.environment.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public interface TaskManagementMemoryListener {
	public void onSubTaskAdded(SubTaskId subTaskId);
	public void onSubTaskFinished(SubTaskId subTaskId);
	public void setPlatformId(PlatformId id);
}
