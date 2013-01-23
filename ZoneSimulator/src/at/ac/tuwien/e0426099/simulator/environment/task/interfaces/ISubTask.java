package at.ac.tuwien.e0426099.simulator.environment.task.interfaces;

import at.ac.tuwien.e0426099.simulator.environment.platform.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 09.12.12
 */
public interface ISubTask {
	public enum SubTaskStatus {NOT_STARTED,RUNNING,PAUSED,FINISHED,SCHEDULING_ERROR, SIMULATED_ERROR,CONCURRENT_ERROR,CANCELING}
	public enum TaskType{PROCESSING,NETWORK_IO,DISK_IO}

	public SubTaskId getSubTaskId();
	public String getReadAbleName();
	public void setParentId(UUID id);
	public PlatformId getPlatformId();
	public void setPlatformId(PlatformId id);

	public void pause();
	public void run();
	public void fail(Exception e);

	public SubTaskStatus getStatus();
	public TaskType getTaskType();

	public void addTaskListener(ITaskListener listener);

	public void waitForTaskToFinish();
    public String getCompleteStatus(boolean detailed);
}
