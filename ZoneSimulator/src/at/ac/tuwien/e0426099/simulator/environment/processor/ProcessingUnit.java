package at.ac.tuwien.e0426099.simulator.environment.processor;

import at.ac.tuwien.e0426099.simulator.environment.Platform;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.CoreDestination;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.TaskManagementListener;
import at.ac.tuwien.e0426099.simulator.environment.processor.scheduler.IScheduler;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingUnit  implements ProcessingUnitListener {

	private List<ProcessingCore> cores;
	private IScheduler scheduler;

	private List<SubTaskId> finnishedSubTasks;
	private List<SubTaskId> failedSubTasks;

	private TaskManagementListener memoryCallBack;

	public ProcessingUnit(IScheduler scheduler, TaskManagementListener memoryCallBack) {
		this.scheduler =scheduler;
		failedSubTasks = new ArrayList<SubTaskId>();
		finnishedSubTasks = new ArrayList<SubTaskId>();
		this.memoryCallBack = memoryCallBack;
	}

	public void addTask(SubTaskId subTaskId) {
		memoryCallBack.onSubTaskAdded(subTaskId);
		scheduler.addToQueue(subTaskId);
		scheduleTasks();
	}

	@Override
	public void onTaskFinished(ProcessingCore c, SubTaskId subTaskId) {
		memoryCallBack.onSubTaskAdded(subTaskId);
		finnishedSubTasks.add(subTaskId);
		scheduleTasks();
	}

	public List<SubTaskId> getFinnishedSubTasks() {
		return finnishedSubTasks;
	}

	public List<SubTaskId> getFailedSubTasks() {
		return failedSubTasks;
	}

	/* ********************************************************************************** PRIVATES */

	private void scheduleTasks() {
		CoreDestination dest;
		List<ProcessingCoreInfo> coreInfos = getAllInfos();

		while((dest=scheduler.getNext(coreInfos)) != null) {
			try {
				addTaskToDestination(dest);
			} catch (TooMuchConcurrentTasksException e) {
				Platform.getInstance().getSubTask(dest.getSubTaskId()).fail(e);
				failedSubTasks.add(dest.getSubTaskId());
			}
		}
	}

	private void addTaskToDestination(CoreDestination dest) throws TooMuchConcurrentTasksException {
		for(ProcessingCore core: cores) {
			if(core.getId().equals(dest.getCoreId())) {
				core.addTask(dest.getSubTaskId());
			}
		}
	}

	private List<ProcessingCoreInfo> getAllInfos() {
		List<ProcessingCoreInfo> list = new ArrayList<ProcessingCoreInfo>();
		for(ProcessingCore core: cores) {
			list.add(core.getInfo());
		}
		return list;
	}
}
