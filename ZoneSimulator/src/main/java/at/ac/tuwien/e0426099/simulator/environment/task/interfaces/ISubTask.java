package at.ac.tuwien.e0426099.simulator.environment.task.interfaces;

import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;

import java.util.UUID;

/**
 * Interface for a subtask. An task is a container for subtasks, that have certain attributes and needs.
 *
 * @author PatrickF
 * @since 09.12.12
 */
public interface ISubTask {
	public enum SubTaskStatus {NOT_STARTED,RUNNING,PAUSED,FINISHED,SCHEDULING_ERROR, SIMULATED_ERROR,CONCURRENT_ERROR}
	public enum TaskType{PROCESSING,NETWORK_IO,DISK_IO}

	/**
	 * This will have a "has-no-parent-task" id as parent id, if not set
	 * @return current id
	 */
	public SubTaskId getSubTaskId();

	/**
	 * Has no importance to the execution, just for easier human readability
	 * @return
	 */
	public String getReadAbleName();

	/**
	 * When a subtask is added to a task, this will be called
	 * @param id
	 */
	public void setParentId(UUID id);

	/**
	 * Get the zone ref, where its executed
	 * @param id
	 */
	public void setZoneId(ZoneId id);

	/**
	 * The execution factor will be used to divide the computed normal exec time (e.g. 2.0 will
	 * make the exec twice as fast)
	 * @param factor
	 */
	public void setExecutionFactor(double factor);

	/**
	 * Will pause the current execution
	 */
	public void pause();

	/**
	 * Will run or resume on pause
	 */
	public void run();

	/**
	 * If this is called, the task stops its execution and saves this as error
	 * @param e
	 */
	public void fail(Exception e);

	/**
	 * Current status, like RUNNING, PAUSE, etc.
	 * @return
	 */
	public SubTaskStatus getStatus();

	/**
	 * Returns the type like for cpu, network, etc.
	 * @return
	 */
	public TaskType getTaskType();

	/**
	 * Adds a tasklistener which gets informed on cases like task finished etc.
	 * @param listener
	 */
	public void addTaskListener(ITaskListener listener);

	/**
	 * A verbose status string
	 * @param detailed
	 * @return
	 */
    public String getCompleteStatus(boolean detailed);
}
