package at.ac.tuwien.e0426099.simulator.environment.task.interfaces;

import at.ac.tuwien.e0426099.simulator.environment.platform.PlatformId;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public interface ITask {
	public enum TaskStatus {NOT_STARTED,IN_PROGRESS,FINISHED,ERROR}

	public UUID getId();
	public String getReadAbleName();
	public void setPlatformId(PlatformId id);

	public void addSubTask(ISubTask subTask);
	public ISubTask getNextSubTask();
	public ISubTask getCurrentSubTask();
	public ISubTask getSubTaskById(UUID id);
	public boolean subTasksLeftToDo();
	public TaskStatus getTaskStatus();
	public void registerFailedSubTask(UUID subTaskId);


    /**
     * Returns true if this task has finished its exection,
     * either if its finished or in error mode.
     *
     * @return
     */
    public boolean isFinishedExecuting();
}
