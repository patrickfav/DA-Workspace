package at.ac.tuwien.e0426099.simulator.environment.task.interfaces;

import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;

/**
 * @author PatrickF
 * @since 09.12.12
 */
public interface ISubTask {
	public enum TaskStatus{NOT_STARTED,RUNNING,PAUSED,FINISHED,SCHEDULING_ERROR, SIMULATED_ERROR,CONCURRENT_ERROR}
	public enum TaskType{PROCESSING,NETWORK_IO,DISK_IO}

	public SubTaskId getSubTaskId();
	public String getReadAbleName();

	public void pause();
	public void run();
	public void fail(Exception e);

	public TaskStatus getStatus();
	public TaskType getTaskType();

	public void addTaskListener(ITaskListener listener);
}
