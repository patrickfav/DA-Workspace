package at.ac.tuwien.e0426099.simulator.environment.processor.scheduler;

import at.ac.tuwien.e0426099.simulator.environment.processor.entities.CoreDestination;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.task.IRunnableTask;
import at.ac.tuwien.e0426099.simulator.environment.task.comparator.ProcessingInfoLoadComparator;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class FifoLeastLoadScheduler implements IScheduler{

	private Queue<IRunnableTask> taskQueue;

	public FifoLeastLoadScheduler() {
		taskQueue=new ConcurrentLinkedQueue<IRunnableTask>();
	}

	@Override
	public void addToQueue(IRunnableTask task) {
		taskQueue.offer(task);
	}

	@Override
	public CoreDestination getNext(List<ProcessingCoreInfo> coreInfos) {
		if(coreInfos != null && coreInfos.size() > 0) {
			Collections.sort(coreInfos,new ProcessingInfoLoadComparator());
			return new CoreDestination(coreInfos.get(0).getId(),taskQueue.peek());
		}
		return null;
	}
}
