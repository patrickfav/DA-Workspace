package at.ac.tuwien.e0426099.simulator.environment.platform.processor.listener;

import at.ac.tuwien.e0426099.simulator.environment.platform.processor.ProcessingCore;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public interface ProcessingUnitListener {
	public void onTaskFinished(ProcessingCore c,SubTaskId subTaskId);
	public void onTaskFailed(ProcessingCore c,SubTaskId subTaskId);
}
