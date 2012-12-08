package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public interface ISubTask {
	public enum TaskStatus{NOT_STARTED,RUNNING,PAUSED,FINISHED,SCHEDULING_ERROR,ERROR}
	public enum TaskType{PROCESSING,NETWORK_IO,DISK_IO}

	public SubTaskId getId();
	public String getReadAbleName();

	public void pause();
	public void run();
	public void fail(Exception e);

	public void updateResources(long resources);

	public long getCurrentlyAssignedProcessingPower();

	public TaskStatus getStatus();
	public TaskType getTaskType();
	public ProcessingRequirements getProcessingRequirements();
	public MemoryAmount getMemoryDemand();

	public void setTaskListener(ITaskListener listener);
}
