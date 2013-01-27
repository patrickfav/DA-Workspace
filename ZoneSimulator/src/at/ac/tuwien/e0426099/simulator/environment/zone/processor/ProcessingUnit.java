package at.ac.tuwien.e0426099.simulator.environment.zone.processor;

import at.ac.tuwien.e0426099.simulator.environment.Env;
import at.ac.tuwien.e0426099.simulator.environment.abstracts.APauseAbleThread;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.ActionWrapper;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.CoreDestination;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.listener.TaskManagementMemoryListener;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.scheduler.IScheduler;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;
import at.ac.tuwien.e0426099.simulator.helper.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A simulation of a CPU, which pretty much is responsibly for scheduling tasks upon its cores
 *
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingUnit extends APauseAbleThread<ActionWrapper> implements ProcessingUnitListener {

	private ZoneId zoneId;
	private List<ProcessingCore> cores;
	private IScheduler scheduler;

	private List<SubTaskId> finnishedSubTasks;
	private List<SubTaskId> failedSubTasks;

	private TaskManagementMemoryListener memoryCallBack;
	private ProcessingUnitListener platformCallBack;

	public ProcessingUnit(IScheduler scheduler, TaskManagementMemoryListener memoryCallBack,List<ProcessingCore> cores) {
		this.scheduler =scheduler;
		failedSubTasks = new ArrayList<SubTaskId>();
		finnishedSubTasks = new ArrayList<SubTaskId>();
		this.memoryCallBack = memoryCallBack;
		this.cores=cores;

		for(int i=0;i<cores.size();i++) {
			cores.get(i).setProcessingUnitListener(this);
			cores.get(i).setCoreName("C" + String.valueOf(i));
		}
		getLog().refreshData();
	}

	public void addTask(SubTaskId subTaskId) {
		getLog().d("adding subtask to cpu "+subTaskId);
		memoryCallBack.onSubTaskAdded(subTaskId);
        addToWorkerQueue(new ActionWrapper(subTaskId, ActionWrapper.ActionType.ADD));
	}

	public synchronized void setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
		this.memoryCallBack.setZoneId(zoneId);
		for(int i=0;i<cores.size();i++) {
			cores.get(i).setZoneId(zoneId);
		}
		getLog().refreshData();
	}

	public synchronized void setPlatformCallBack(ProcessingUnitListener platformCallBack) {
		this.platformCallBack = platformCallBack;
	}

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.BR+LogUtil.h3("CPU "+toString()));
        for(ProcessingCore core:cores) {
            sb.append(core.getCompleteStatus(detailed) + LogUtil.BR);
        }
        sb.append(LogUtil.BR +LogUtil.h3("Finished Subtasks"));
        sb.append(LogUtil.emptyListText(finnishedSubTasks," - no tasks -"));
        for(SubTaskId id:finnishedSubTasks) {
            sb.append(Env.get().getZone(zoneId).getSubTaskForProcessor(id).getCompleteStatus(false)+LogUtil.BR);
        }

        sb.append(LogUtil.BR +LogUtil.h3("Failed Subtasks"));
        sb.append(LogUtil.emptyListText(failedSubTasks, " - no tasks -"));
        for(SubTaskId id:failedSubTasks) {
            sb.append(Env.get().getZone(zoneId).getSubTaskForProcessor(id).getCompleteStatus(false)+LogUtil.BR);
        }
        return sb.toString();
    }

	@Override
	public String toString() {
		return "["+ zoneId +"|CPU]";
	}

 	/* ********************************************************************************** THREAD ABSTRACT IMPL*/
	 @Override
	 public void start() {
		 for(int i=0;i<cores.size();i++) {
			 cores.get(i).setExecutionFactor(getExecutionFactor());
			 cores.get(i).start();
		 }
		 super.start();
	 }

	@Override
	public void doTheWork(ActionWrapper input) {
		getWorkLock().lock();
		if(input.getActionType().equals(ActionWrapper.ActionType.ADD)) {
			scheduler.addToQueue(input.getSubTaskId());
		} else if(input.getActionType().equals(ActionWrapper.ActionType.FINISH)) {
			memoryCallBack.onSubTaskAdded(input.getSubTaskId());
			finnishedSubTasks.add(input.getSubTaskId());
			platformCallBack.onTaskFinished(input.getCore(),input.getSubTaskId());
		} else if(input.getActionType().equals(ActionWrapper.ActionType.FAIL)) {
			failedSubTasks.add(input.getSubTaskId());
			platformCallBack.onTaskFailed(input.getCore(), input.getSubTaskId());
		}
		scheduleTasks();
		getWorkLock().unlock();
	}

	@Override
	public void onAllDone() {
		getLog().d("[Sync] all done callback, stop cores");
		for(ProcessingCore c : cores) {
			c.stopExec();
			c.interrupt(); //interrupt blocking queue
		}
		for(ProcessingCore c : cores) {
			c.waitForFinish();
		}
	}

	@Override
	public void pause() {
		super.pause();
		for(ProcessingCore c : cores) {
			c.pause();
		}
	}

	@Override
	public void resumeExec() {
		super.resumeExec();
		for(ProcessingCore c : cores) {
			c.setExecutionFactor(getExecutionFactor());
			c.resumeExec();
		}
	}
	/* ********************************************************************************** CALLBACKS */

	@Override
	public void onTaskFinished(ProcessingCore c, SubTaskId subTaskId) {
		getLog().d("task finished in "+c.getCoreName()+", spreading the word: "+subTaskId);
		addToWorkerQueue(new ActionWrapper(subTaskId, ActionWrapper.ActionType.FINISH));
	}

	@Override
	public void onTaskFailed(ProcessingCore c, SubTaskId subTaskId) {
		getLog().d("task failed in "+c.getCoreName()+", spreading the word: "+subTaskId);
		addToWorkerQueue(new ActionWrapper(subTaskId, ActionWrapper.ActionType.FAIL));
	}

	/* ********************************************************************************** PRIVATES */

	private void scheduleTasks() {
		getLog().d("scheduling tasks between cores");
		CoreDestination dest;

		while((dest=scheduler.getNext(getAllInfos())) != null) {
			try {
				getLog().d("next task to schedule: "+ Env.get().getZone(zoneId).getSubTaskForProcessor(dest.getSubTaskId()));
				addTaskToDestination(dest);
			} catch (TooMuchConcurrentTasksException e) {
				getLog().w("too many concurrent tasks! failing task.",e);
				Env.get().getZone(zoneId).getSubTaskForProcessor(dest.getSubTaskId()).fail(e);
				failedSubTasks.add(dest.getSubTaskId());
			}
		}
	}

	private void addTaskToDestination(CoreDestination dest) throws TooMuchConcurrentTasksException {
		for(ProcessingCore core: cores) {
			if(core.getCoreId().equals(dest.getCoreId())) {
				core.addTask(dest.getSubTaskId());
				break;
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
