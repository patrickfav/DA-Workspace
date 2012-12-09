package at.ac.tuwien.e0426099.simulator.environment;

import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.IComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class Platform {
	private static Platform instance;

	private ExecutorService threadPool;
	private ConcurrentHashMap<UUID,ITask> taskMap;

	public static Platform getInstance() {
		if(instance == null) {
			instance = new Platform();
		}
		return instance;
	}

	private Platform() {
		taskMap = new ConcurrentHashMap<UUID,ITask>();
		threadPool = Executors.newCachedThreadPool();
	}

	public ITask getTask(UUID id) {
		if(taskMap.containsKey(id)) {
			return taskMap.get(id);
		} else {
			return null;
		}
	}

	public IComputationalSubTask getSubTask(SubTaskId subTaskId) {
		if(taskMap.containsKey(subTaskId.getParentTaskId())) {
			return taskMap.get(subTaskId.getParentTaskId()).getSubTaskById(subTaskId.getSubTaskId());
		} else {
			return null;
		}
	}

	public IComputationalSubTask getSubTask(UUID taskId, UUID subTaskId) {
		if(taskMap.containsKey(taskId)) {
			return taskMap.get(taskId).getSubTaskById(subTaskId);
		} else {
			return null;
		}
	}

	public synchronized ExecutorService getThreadPool() {
		return threadPool;
	}
}
