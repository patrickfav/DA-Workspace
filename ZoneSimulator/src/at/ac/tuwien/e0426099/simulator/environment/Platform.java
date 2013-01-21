package at.ac.tuwien.e0426099.simulator.environment;

import at.ac.tuwien.e0426099.simulator.environment.abstracts.APauseAbleThread;
import at.ac.tuwien.e0426099.simulator.environment.processor.ProcessingCore;
import at.ac.tuwien.e0426099.simulator.environment.processor.ProcessingUnit;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.IComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class Platform extends APauseAbleThread<UUID> implements ProcessingUnitListener {
	private Logger log = LogManager.getLogger(Platform.class.getName());

	private PlatformId platformId;
	private ExecutorService threadPool;
	private ConcurrentHashMap<UUID, ITask> taskMap;
	private ProcessingUnit processingUnit;

	public Platform(PlatformId platformId, ProcessingUnit unit) {
        this.platformId = platformId;
		taskMap = new ConcurrentHashMap<UUID, ITask>();
		threadPool = Executors.newCachedThreadPool();
		processingUnit = unit;
		processingUnit.setPlatformId(platformId);
		processingUnit.setPlatformCallBack(this);
		processingUnit.start();
	}

	public synchronized void addTask(ITask task) {
		task.setPlatformId(platformId);
		log.info(this + " Add new Task: " + task);
		taskMap.put(task.getId(), task);
        addToWorkerQueue(task.getId());
	}

	public IComputationalSubTask getSubTaskForProcessor(SubTaskId subTaskId) {
		return (IComputationalSubTask) taskMap.get(subTaskId.getParentTaskId()).getSubTaskById(subTaskId.getSubTaskId());
	}

	public synchronized ExecutorService getThreadPool() {
	    return threadPool;
	}

	@Override
	public String toString() {
		return "[Platform|" + platformId + "]";
	}

	public synchronized String getCompleteStatus(boolean detailed) {
		StringBuffer sb = new StringBuffer();
		sb.append(LogUtil.BR + LogUtil.h2("Platform: " + this));
		sb.append(processingUnit.getCompleteStatus(detailed));
		return sb.toString();
	}

    /* ********************************************************************************** THREAD ABSTRACT IMPL*/

    @Override
    public void doTheWork(UUID input) {
        if(taskMap.containsKey(input))
            dispatcher(taskMap.get(input));
    }

    @Override
    public synchronized boolean checkIfThereWillBeAnyWork() {
        boolean isDone = true;
        for (ITask task : taskMap.values()) {
            isDone &= task.isFinishedExecuting();
        }
        return !isDone;
    }
    /* ********************************************************************************** CALLBACKS */

	@Override
	public synchronized void onTaskFinished(ProcessingCore c, SubTaskId subTaskId) {
		addToWorkerQueue(subTaskId.getParentTaskId());
	}

	@Override
	public synchronized void onTaskFailed(ProcessingCore c, SubTaskId subTaskId) {
		taskMap.get(subTaskId.getParentTaskId()).registerFailedSubTask(subTaskId.getSubTaskId());
        addToWorkerQueue(subTaskId.getParentTaskId());
	}


	/* ********************************************************************************** PRIVATES */

	/**
	 * Dispatches next subtask form given task to the correct handler
	 *
	 * @param task
	 * @return true if there was something to dispatch, or false otherwise
	 */
	private boolean dispatcher(ITask task) {
		if (task.subTasksLeftToDo()) {
			log.debug(this + " Start dispatcher with Task " + task);
			ISubTask subTask = task.getNextSubTask();

			if (subTask.getTaskType() == ISubTask.TaskType.PROCESSING) {
				log.debug(this + " Dispatching to Processor");
				processingUnit.addTask(subTask.getSubTaskId());
				return true;
			} else if (subTask.getTaskType() == ISubTask.TaskType.NETWORK_IO) {
				log.debug(this + " Dispatching to Network");
				return true;
			} else if (subTask.getTaskType() == ISubTask.TaskType.DISK_IO) {
				log.debug(this + " Dispatching to Disk");
				return true;
			}
		}
		log.debug(this + " No more subtasks to dispatch in task.");
		return false;
	}


}
