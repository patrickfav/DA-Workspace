package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.Env;
import at.ac.tuwien.e0426099.simulator.environment.EnvConst;
import at.ac.tuwien.e0426099.simulator.environment.task.producer.templates.TaskTemplate;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import at.ac.tuwien.e0426099.simulator.helper.Log;
import at.ac.tuwien.e0426099.simulator.helper.util.LogUtil;

import java.util.*;

/**
 * @author PatrickF
 * @since 13.12.12
 */
public class Task implements ITask{
	private Log log = new Log(this, EnvConst.VERBOSE_LOG_MODE_GENERAL && EnvConst.VERBOSE_LOG_MODE_TASK);

	private UUID id;
	private ZoneId zoneId;
	private String readAbleName;
	private Map<UUID,ISubTask> subTasks;
	private List<UUID> subTaskOrder;
	private int currentSubTask;
	private volatile TaskStatus status;

	public Task(String readAbleName) {
		this.id = UUID.randomUUID();
		this.readAbleName = readAbleName;
		this.subTasks = new HashMap<UUID, ISubTask>();
		subTaskOrder =new ArrayList<UUID>();
		currentSubTask=-1;
		setStatus(TaskStatus.NOT_STARTED);
		log.refreshData();
	}

	public Task(TaskTemplate template) {
		this(template.getReadAblenName());
	}

	public void addSubTask(ISubTask subTask) {
		subTask.setParentId(id);
		subTasks.put(subTask.getSubTaskId().getSubTaskId(), subTask);
		subTaskOrder.add(subTask.getSubTaskId().getSubTaskId());
	}

	@Override
	public ISubTask getNextSubTask() {
		log.d("get next subtask");
		if(subTasksLeftToDo()) {
			setStatus(TaskStatus.IN_PROGRESS);
			ISubTask st = getSubTaskById(subTaskOrder.get(++currentSubTask));
			log.d("return next subtask: "+st);
			return st;
		}
		log.d("no more subtasks");
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
		log.v(subTaskOrder.size()+" > 0 && "+(currentSubTask+1)+" < "+subTaskOrder.size()+": "+(subTaskOrder.size() > 0 && currentSubTask+1 < subTaskOrder.size()));

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
	public void setZoneId(ZoneId id) {
		zoneId =id;
		for(ISubTask st: subTasks.values()) {
			st.setZoneId(id);
		}
		log.refreshData();
	}

    @Override
    public boolean isFinishedExecuting() {
        return status == TaskStatus.FINISHED || status == TaskStatus.ERROR;
    }
	public synchronized String getCompleteStatus(boolean detailed) {
		StringBuffer sb = new StringBuffer();

		sb.append(LogUtil.BR +LogUtil.h4("TASK: "+this.toString()));
		sb.append(LogUtil.emptyListText(subTaskOrder," - no tasks -"));
		if(detailed) {
			for(UUID id:subTaskOrder) {
				sb.append(Env.get().getZone(zoneId).getSubTaskForProcessor(subTasks.get(id).getSubTaskId()).getCompleteStatus(detailed)+LogUtil.BR);
			}
		}
		return sb.toString();
	}
    @Override
    public String toString() {
		return "["+ zoneId +"|Task|"+readAbleName+"/"+id.toString().substring(0,5)+"|"+status+"]";
    }
    /* ***************************************************************************** PRIVATES */

	private void setStatus(TaskStatus newStatus) {
		log.d("Set status from " + status + " to " + newStatus);
		this.status = newStatus;
		log.refreshData();
	}
}
