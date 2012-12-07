package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.memory.entities.KiB;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public interface IRunnableTask {
	public enum TaskStatus{NOT_STARTED,RUNNING,PAUSED,FINISHED,SCHEDULING_ERROR,ERROR}

	public UUID getId();
	public String getReadAbleName();

	public void pause();
	public void run();
	public void fail(Exception e);

	public void updateProcessingResources(long processingPower);

	public long getCurrentlyAssignedProcessingPower();

	public TaskStatus getStatus();
	public ProcessingRequirements getProcessingRequirements();
	public KiB getMemoryDemand();

	public void setTaskListener(ITaskListener listener);
}
