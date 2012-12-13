package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;

import java.util.*;

/**
 * @author PatrickF
 * @since 13.12.12
 */
public class Task implements ITask{

	private UUID id;
	private String readAbleName;
	private Map<UUID,ISubTask> subTasks;
	private List<UUID> subTaskOrder;
	private int currentSubTask;
	private TaskStatus status;

	public Task(String readAbleName) {
		this.id = UUID.randomUUID();
		this.readAbleName = readAbleName;
		this.subTasks = new HashMap<UUID, ISubTask>();
		subTaskOrder =new ArrayList<UUID>();
		currentSubTask=-1;
		status = TaskStatus.NOT_STARTED;
	}

	public void addSubTask(ISubTask subTask) {
		subTask.setParentId(id);
		subTasks.put(subTask.getSubTaskId().getSubTaskId(),subTask);
		subTaskOrder.add(subTask.getSubTaskId().getSubTaskId());
	}

	@Override
	public ISubTask getNextSubTask() {
		if(subTasksLeftToDo()) {
			status = TaskStatus.IN_PROGRESS;
			return getSubTaskById(subTaskOrder.get(++currentSubTask));
		}
		status = TaskStatus.FINISHED;
		return null;
	}
	@Override
	public ISubTask getCurrentSubTask() {
		if(currentSubTask >=0 )
			return getSubTaskById(subTaskOrder.get(currentSubTask));
		return null;
	}
	@Override
	public boolean subTasksLeftToDo() {
		return subTaskOrder.size() > 0 && currentSubTask+1 < subTaskOrder.size();
	}

	@Override
	public TaskStatus getTaskStatus() {
		return status;
	}

	@Override
	public ISubTask getSubTaskById(UUID id) {
		if(subTasks.containsKey(id)) {
			return subTasks.get(id);
		}
		return null;
	}

	@Override
	public UUID getId() {
		return id;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getReadAbleName() {
		return readAbleName;
	}

	@Override
	public String toString() {
		return "Task{" +
				"name=" + readAbleName +
				", id='" + id + '\'' +
				", status=" + status +
				'}';
	}
}
