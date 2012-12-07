package at.ac.tuwien.e0426099.simulator.environment.processor;

import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.CoreDestination;
import at.ac.tuwien.e0426099.simulator.environment.processor.scheduler.IScheduler;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.task.IRunnableTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingUnit  implements ProcessingCore.ProcessingUnitListener {

	private List<ProcessingCore> cores;
	private IScheduler scheduler;

	private List<IRunnableTask> finnishedTasks;
	private List<IRunnableTask> failedTasks;

	public ProcessingUnit(IScheduler scheduler) {
		this.scheduler =scheduler;
		failedTasks = new ArrayList<IRunnableTask>();
		finnishedTasks = new ArrayList<IRunnableTask>();
	}

	public void addTask(IRunnableTask task) {
		scheduler.addToQueue(task);
		scheduleTasks();
	}

	@Override
	public void onTaskFinished(ProcessingCore c, IRunnableTask t) {
		finnishedTasks.add(t);
		scheduleTasks();
	}

	public List<IRunnableTask> getFinnishedTasks() {
		return finnishedTasks;
	}

	public List<IRunnableTask> getFailedTasks() {
		return failedTasks;
	}

	/* ********************************************************************************** PRIVATES */

	private void scheduleTasks() {
		CoreDestination dest;
		List<ProcessingCoreInfo> coreInfos = getAllInfos();

		while((dest=scheduler.getNext(coreInfos)) != null) {
			try {
				addTaskToDestination(dest);
			} catch (TooMuchConcurrentTasksException e) {
				dest.getTask().fail(e);
				failedTasks.add(dest.getTask());
			}
		}
	}

	private void addTaskToDestination(CoreDestination dest) throws TooMuchConcurrentTasksException {
		for(ProcessingCore core: cores) {
			if(core.getId().equals(dest.getCoreId())) {
				core.addTask(dest.getTask());
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
