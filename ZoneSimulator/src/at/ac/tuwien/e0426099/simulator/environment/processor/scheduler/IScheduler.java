package at.ac.tuwien.e0426099.simulator.environment.processor.scheduler;

import at.ac.tuwien.e0426099.simulator.environment.processor.entities.CoreDestination;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

import java.util.List;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public interface IScheduler {
	public void addToQueue(SubTaskId subTaskId);
	public CoreDestination getNext(List<ProcessingCoreInfo> coreInfos);
}
