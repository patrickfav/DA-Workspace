package at.ac.tuwien.e0426099.simulator.environment;

import at.ac.tuwien.e0426099.simulator.environment.processor.ProcessingCore;
import at.ac.tuwien.e0426099.simulator.environment.processor.ProcessingUnit;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.IComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
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
	private static Platform instance;

	private String platformName;
	private ExecutorService threadPool;
	private ConcurrentHashMap<UUID,ITask> taskMap;

	private ProcessingUnit processingUnit;

	private boolean setUp = false;

	public static Platform instance() {
		if(instance == null) {
			instance = new Platform();
		}
		return instance;
	}

	private Platform() {
		taskMap = new ConcurrentHashMap<UUID,ITask>();
		threadPool = Executors.newCachedThreadPool();
	}

	public void addTask(ITask task) {
		log.info(this+" Add new Task: "+task);
		checkSetup();
		taskMap.put(task.getId(),task);
		dispatcher(task);
	}

	public ITask getTask(UUID id) {
		if(taskMap.containsKey(id)) {
			return taskMap.get(id);
		} else {
			return null;
		}
	}

	public IComputationalSubTask getSubTaskForProcessor(SubTaskId subTaskId) {
		if(taskMap.containsKey(subTaskId.getParentTaskId())) {
			return (IComputationalSubTask) taskMap.get(subTaskId.getParentTaskId()).getSubTaskById(subTaskId.getSubTaskId());
		} else {
			return null;
		}
	}

	public ISubTask getSubTask(SubTaskId subTaskId) {
		if(taskMap.containsKey(subTaskId.getParentTaskId())) {
			return taskMap.get(subTaskId.getParentTaskId()).getSubTaskById(subTaskId.getSubTaskId());
		} else {
			return null;
		}
	}

	public synchronized ExecutorService getThreadPool() {
		return threadPool;
	}

	public void setUp(String platformName, ProcessingUnit unit) {
		processingUnit = unit;
		//processingUnit.start();
		this.platformName = platformName;
		setUp =true;

	}

	private void checkSetup() {
		if(!setUp) {
			throw new RuntimeException("Cannot use Platform if it wasn't set up properly. Please call setUp first.");
		}
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
		if(!dispatcher(taskMap.get(subTaskId.getParentTaskId()))) {
			log.debug(this+" Waiting for tasks to finish...");
			for(ITask task:taskMap.values()) {
				task.blockWaitUntilFinished();
			}
			log.debug(this+" All done.");
		}
	}

	@Override
	public String toString() {
		return "["+platformName+"]";
	}
}
