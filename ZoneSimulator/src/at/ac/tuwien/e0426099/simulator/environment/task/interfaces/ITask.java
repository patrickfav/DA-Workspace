package at.ac.tuwien.e0426099.simulator.environment.task.interfaces;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public interface ITask {
	public enum TaskStatus {NOT_STARTED,IN_PROGRESS,FINISHED,ERROR}

	public UUID getId();
	public String getReadAbleName();


	public void addSubTask(ISubTask subTask);
	public ISubTask getNextSubTask();
	public ISubTask getCurrentSubTask();
	public ISubTask getSubTaskById(UUID id);
	public boolean subTasksLeftToDo();
	public TaskStatus getTaskStatus();

	public void blockWaitUntilFinished();

}
