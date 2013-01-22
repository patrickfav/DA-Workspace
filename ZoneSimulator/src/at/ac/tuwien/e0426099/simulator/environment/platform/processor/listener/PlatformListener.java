package at.ac.tuwien.e0426099.simulator.environment.platform.processor.listener;

import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

/**
 * @author PatrickF
 * @since 13.12.12
 */
public interface PlatformListener {
	public void processorHasFinishedSubTask(SubTaskId id);
}
