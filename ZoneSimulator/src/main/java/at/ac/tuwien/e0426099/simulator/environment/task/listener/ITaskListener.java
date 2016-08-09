package at.ac.tuwien.e0426099.simulator.environment.task.listener;

import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public interface ITaskListener {
	public void onTaskFinished(SubTaskId task);
	public void onTaskFailed(SubTaskId task);
	public UUID getCoreId();
}
