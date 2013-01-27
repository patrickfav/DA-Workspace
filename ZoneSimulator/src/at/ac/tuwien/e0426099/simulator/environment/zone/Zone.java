package at.ac.tuwien.e0426099.simulator.environment.zone;

import at.ac.tuwien.e0426099.simulator.environment.abstracts.APauseAbleThread;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.ProcessingCore;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.ProcessingUnit;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.IComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import at.ac.tuwien.e0426099.simulator.helper.util.LogUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A zone represents a unit of execution, including a cpu, network, memory etc.
 * Comparable to a single VM.
 *
 * @author PatrickF
 * @since 08.12.12
 */
public class Zone extends APauseAbleThread<UUID> implements ProcessingUnitListener {

	private ZoneId zoneId;
	private ExecutorService threadPool;
	private ConcurrentHashMap<UUID, ITask> taskMap;
	private ProcessingUnit processingUnit;

	public Zone(ZoneId zoneId, ProcessingUnit unit) {
		this.zoneId = zoneId;
		taskMap = new ConcurrentHashMap<UUID, ITask>();
		threadPool = Executors.newCachedThreadPool();
		processingUnit = unit;
		processingUnit.setZoneId(zoneId);
		processingUnit.setPlatformCallBack(this);
		getLog().refreshData();
	}

	public void addTask(ITask task) {
		task.setZoneId(zoneId);
		getLog().i("Add new Task: " + task);
		taskMap.put(task.getId(), task);
		addToWorkerQueue(task.getId());
	}

	public IComputationalSubTask getSubTaskForProcessor(SubTaskId subTaskId) {
		return (IComputationalSubTask) taskMap.get(subTaskId.getParentTaskId()).getSubTaskById(subTaskId.getSubTaskId());
	}

	public ExecutorService getThreadPool() {
		return threadPool;
	}

	@Override
	public String toString() {
		return "[Zone|" + zoneId + "]";
	}

	public synchronized String getCompleteStatus(boolean detailed) {
		StringBuffer sb = new StringBuffer();
		sb.append(LogUtil.BR + LogUtil.h2("Zone: " + this));
		for(ITask task: taskMap.values()) {
			sb.append(task.getCompleteStatus(detailed)+LogUtil.BR);
		}

		sb.append(processingUnit.getCompleteStatus(detailed));
		return sb.toString();
	}

    /* ********************************************************************************** THREAD ABSTRACT IMPL*/
	@Override
	public void start() {
		processingUnit.setExecutionFactor(getExecutionFactor());
		processingUnit.start();
		super.start();
	}

	@Override
	public void doTheWork(UUID input) {
		getWorkLock().lock();
		if (input != null && taskMap.containsKey(input))
			dispatcher(taskMap.get(input));
		getWorkLock().unlock();
	}

	@Override
	public boolean checkIfThereWillBeAnyWork() {
		boolean isDone = true;
		for (ITask task : taskMap.values()) {
			isDone &= task.isFinishedExecuting();
		}

		getLog().d("[Sync] checkIfThereWillBeAnyWork: " + String.valueOf(!isDone));
		return !isDone;
	}

	@Override
	public synchronized void onAllDone() {
		getLog().d("[Sync] all done callback, stop cpu");
		processingUnit.stopExec();
		processingUnit.interrupt(); //interrupt blocking queue
		processingUnit.waitForFinish();
	}

	@Override
	public synchronized void pause() {
		super.pause();
		processingUnit.pause();
	}

	@Override
	public synchronized void resumeExec() {
		super.resumeExec();
		processingUnit.setExecutionFactor(getExecutionFactor());
		processingUnit.resumeExec();
	}


	/* ********************************************************************************** CALLBACKS */

	@Override
	public void onTaskFinished(ProcessingCore c, SubTaskId subTaskId) {
		addToWorkerQueue(subTaskId.getParentTaskId());
	}

	@Override
	public void onTaskFailed(ProcessingCore c, SubTaskId subTaskId) {
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
			getLog().d("Start dispatcher with Task " + task);
			ISubTask subTask = task.getNextSubTask();

			if (subTask.getTaskType() == ISubTask.TaskType.PROCESSING) {
				getLog().d("Dispatching to Processor");
				processingUnit.addTask(subTask.getSubTaskId());
				return true;
			} else if (subTask.getTaskType() == ISubTask.TaskType.NETWORK_IO) {
				getLog().d("Dispatching to Network");
				return true;
			} else if (subTask.getTaskType() == ISubTask.TaskType.DISK_IO) {
				getLog().d("Dispatching to Disk");
				return true;
			}
		}
		getLog().d("No more subtasks to dispatch in task.");
		return false;
	}


}
