package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author PatrickF
 * @since 13.12.12
 */
public class Task implements ITask{
	private Logger log = LogManager.getLogger(Task.class.getName());

	private UUID id;
	private PlatformId platformId;
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
		setStatus(TaskStatus.NOT_STARTED);
	}

	public void addSubTask(ISubTask subTask) {
		subTask.setParentId(id);
		subTasks.put(subTask.getSubTaskId().getSubTaskId(),subTask);
		subTaskOrder.add(subTask.getSubTaskId().getSubTaskId());
	}

	@Override
	public ISubTask getNextSubTask() {
		if(subTasksLeftToDo()) {
			setStatus(TaskStatus.IN_PROGRESS);
			return getSubTaskById(subTaskOrder.get(++currentSubTask));
		}
		setStatus(TaskStatus.FINISHED);
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
		if(G.VERBOSE_LOG_MODE)
			log.debug(getLogRef()+subTaskOrder.size()+" > 0 && "+(currentSubTask+1)+" < "+subTaskOrder.size()+": "+(subTaskOrder.size() > 0 && currentSubTask+1 < subTaskOrder.size()));

		if(subTaskOrder.size() > 0 && currentSubTask+1 < subTaskOrder.size()) {
			return true;
		} else {
			setStatus(TaskStatus.FINISHED);
			return false;
		}
	}

	@Override
	public TaskStatus getTaskStatus() {
		return status;
	}

	@Override
	public void registerFailedSubTask(UUID subTaskId) {
		setStatus(TaskStatus.ERROR);
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
		return id;
	}

	@Override
	public String getReadAbleName() {
		return readAbleName;
	}

	@Override
	public void setPlatformId(PlatformId id) {
		platformId=id;
		for(ISubTask st: subTasks.values()) {
			st.setPlatformId(id);
		}
	}

	/*@Override
	public void blockWaitUntilFinished() {
		for (UUID id : subTaskOrder) {
			if(subTasks.get(id).getStatus() == ISubTask.SubTaskStatus.RUNNING) {
				try {
					subTasks.get(id).waitForTaskToFinish();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}*/

    @Override
    public boolean isFinishedExecuting() {
        return status == TaskStatus.FINISHED || status == TaskStatus.ERROR;
    }

    @Override
    public String toString() {
        return  readAbleName+"/" + id.toString().substring(0,5)+" (status=" + status+")";
    }
    /* ***************************************************************************** PRIVATES */

	private void setStatus(TaskStatus newStatus) {
		log.debug(getLogRef() + "Set status from " + status + " to " + newStatus);
		this.status = newStatus;
	}

	private String getLogRef() {
		return "["+platformId+"|Task|"+readAbleName+"]: ";
	}


}
