package at.ac.tuwien.e0426099.simulator.environment;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class Platform implements ProcessingUnitListener {
	private Logger log = LogManager.getLogger(Platform.class.getName());

	private PlatformId platformId;
	private ExecutorService threadPool;
	private ConcurrentHashMap<UUID,ITask> taskMap;

	private ProcessingUnit processingUnit;


	public Platform(PlatformId platformId, ProcessingUnit unit) {
		this.platformId = platformId;
		taskMap = new ConcurrentHashMap<UUID,ITask>();
		threadPool = Executors.newCachedThreadPool();
		processingUnit = unit;
		processingUnit.setPlatformId(platformId);
		processingUnit.setPlatformCallBack(this);
		//processingUnit.start();
	}


	public void addTask(ITask task) {
		task.setPlatformId(platformId);
		log.info(this+" Add new Task: "+task);
		taskMap.put(task.getId(),task);
		dispatcher(task);
	}

	public ITask getTask(UUID id) {
		return taskMap.get(id);
	}

	public IComputationalSubTask getSubTaskForProcessor(SubTaskId subTaskId) {
		return (IComputationalSubTask) taskMap.get(subTaskId.getParentTaskId()).getSubTaskById(subTaskId.getSubTaskId());
	}

	public ISubTask getSubTask(SubTaskId subTaskId) {
		return taskMap.get(subTaskId.getParentTaskId()).getSubTaskById(subTaskId.getSubTaskId());
	}

	public synchronized ExecutorService getThreadPool() {
		return threadPool;
	}

	private boolean dispatcher(ITask task) {
		if(task.subTasksLeftToDo()) {
			log.debug(this+" Start dispatcher with Task "+task);
			ISubTask subTask = task.getNextSubTask();

			if(subTask.getTaskType() == ISubTask.TaskType.PROCESSING) {
				log.debug(this+" Dispatching to Processor");
				processingUnit.addTask(subTask.getSubTaskId());
				return true;
			} else if(subTask.getTaskType() == ISubTask.TaskType.NETWORK_IO){
				log.debug(this+" Dispatching to Network");
				return true;
			} else if(subTask.getTaskType() == ISubTask.TaskType.DISK_IO){
				log.debug(this+" Dispatching to Disk");
				return true;
			}
		}
		log.debug(this+" No more subtasks to dispatch in task.");
		return false;
	}

	@Override
	public void onTaskFinished(ProcessingCore c, SubTaskId subTaskId) {
		dispatchTasks(subTaskId);
	}

	@Override
	public void onTaskFailed(ProcessingCore c, SubTaskId subTaskId) {
		taskMap.get(subTaskId.getParentTaskId()).registerFailedSubTask(subTaskId.getSubTaskId());
		dispatchTasks(subTaskId);
	}

	private void dispatchTasks(SubTaskId subTaskId) {
		if(!dispatcher(taskMap.get(subTaskId.getParentTaskId()))) {
			/*log.debug(this+" Waiting for tasks to finish...");
			/*for(ITask task:taskMap.values()) {
				task.blockWaitUntilFinished();

			}
			try {
				threadPool.awaitTermination(20, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			//log.debug(this+" Nothing more to dispatch right now.");
		}
	}

	@Override
	public String toString() {
		return "[Platform|"+ platformId +"]";
	}

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.BR+LogUtil.h2("Platform: "+this));
        sb.append(processingUnit.getCompleteStatus(detailed));
        return sb.toString();
    }
}
