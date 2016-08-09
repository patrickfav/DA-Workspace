package at.ac.tuwien.e0426099.simulator.environment.task.interfaces;

import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;

import java.util.UUID;

/**
 * Interface of an Task which contains serveral subtasks.
 * @author PatrickF
 * @since 08.12.12
 */
public interface ITask {
	public enum TaskStatus {NOT_STARTED,IN_PROGRESS,FINISHED,ERROR}

	/**
	 * The id of this task, will be identifyed by this in a zone
	 * @return
	 */
	public UUID getId();

	/**
	 * Has no importance to the execution, just for easier human readability
	 * @return
	 */
	public String getReadAbleName();

	/**
	 * After added to a zone, this will get called
	 * @param id
	 */
	public void setZoneId(ZoneId id);

	/**
	 * Adds a subtask to the complete execution cycle
	 * @param subTask
	 */
	public void addSubTask(ISubTask subTask);

	/**
	 * Gets the next Subtask.
	 * If its called the first time, it will get first subtask, etc
	 *
	 * Does not check subtask's status.
	 *
	 * @return null if there are no more subtasks
	 */
	public ISubTask getNextSubTask();

	/**
	 * Get current subtask (e.g getting the same as calling getNextSubTask
	 * without increasing the counter)
	 *
	 * @return null if getNextSubTask() hasn't been called or getNextSubTask() returns null
	 */
	public ISubTask getCurrentSubTask();

	/**
	 * Get subtask by its id
	 * @param id
	 * @return
	 */
	public ISubTask getSubTaskById(UUID id);

	/**
	 * Returns the status of getNextSubTask() if all subtask have been cycled through
	 * @return true if getNextSubTask() won't return null
	 */
	public boolean subTasksLeftToDo();

	/**
	 * Gets execution status like in-progress or finished
	 * @return
	 */
	public TaskStatus getTaskStatus();

	/**
	 * Callback for the Zone to register when a subtask has failed
	 * @param subTaskId
	 */
	public void registerFailedSubTask(UUID subTaskId);

	/**
	 * A verbose status string
	 * @param detailed
	 * @return
	 */
	public String getCompleteStatus(boolean detailed);

    /**
     * Returns true if this task has finished its exection,
     * either if its finished or in error mode.
     *
     * @return
     */
    public boolean isFinishedExecuting();
}
