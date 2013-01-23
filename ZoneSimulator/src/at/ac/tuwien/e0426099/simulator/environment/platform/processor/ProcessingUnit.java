package at.ac.tuwien.e0426099.simulator.environment.platform.processor;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.abstracts.APauseAbleThread;
import at.ac.tuwien.e0426099.simulator.environment.platform.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.CoreDestination;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.listener.TaskManagementMemoryListener;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.scheduler.IScheduler;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingUnit extends APauseAbleThread<SubTaskId> implements ProcessingUnitListener {

	private PlatformId platformId;
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
			cores.get(i).start();
		}
		getLog().refreshData();
	}

	public void addTask(SubTaskId subTaskId) {
		getLog().d("adding subtask to cpu "+subTaskId);
		memoryCallBack.onSubTaskAdded(subTaskId);
		scheduler.addToQueue(subTaskId);
        addToWorkerQueue(subTaskId);
	}

	public synchronized void setPlatformId(PlatformId platformId) {
		this.platformId = platformId;
		this.memoryCallBack.setPlatformId(platformId);
		for(int i=0;i<cores.size();i++) {
			cores.get(i).setPlatformId(platformId);
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
            sb.append(G.get().getPlatform(platformId).getSubTaskForProcessor(id).getCompleteStatus(detailed)+LogUtil.BR);
        }

        sb.append(LogUtil.BR +LogUtil.h3("Failed Subtasks"));
        sb.append(LogUtil.emptyListText(failedSubTasks, " - no tasks -"));
        for(SubTaskId id:failedSubTasks) {
            sb.append(G.get().getPlatform(platformId).getSubTaskForProcessor(id).getCompleteStatus(detailed)+LogUtil.BR);
        }
        return sb.toString();
    }

	@Override
	public String toString() {
		return "["+platformId+"|CPU]";
	}
	/* ********************************************************************************** CALLBACKS */

    @Override
    public void onTaskFinished(ProcessingCore c, SubTaskId subTaskId) {
		getLog().d("task finished in "+c.getCoreName()+", spreading the word: "+subTaskId);
		memoryCallBack.onSubTaskAdded(subTaskId);
		finnishedSubTasks.add(subTaskId);
		addToWorkerQueue(subTaskId);
        platformCallBack.onTaskFinished(c,subTaskId);
    }

    @Override
    public void onTaskFailed(ProcessingCore c, SubTaskId subTaskId) {
		failedSubTasks.add(subTaskId);
        addToWorkerQueue(subTaskId);
        platformCallBack.onTaskFailed(c, subTaskId);
    }

 	/* ********************************************************************************** THREAD ABSTRACT IMPL*/
	@Override
	public void doTheWork(SubTaskId input) {
		scheduleTasks();
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
			c.resumeExec();
		}
	}
	/* ********************************************************************************** PRIVATES */

	private synchronized void scheduleTasks() {
		getLog().d("scheduling tasks between cores");
		CoreDestination dest;

		while((dest=scheduler.getNext(getAllInfos())) != null) {
			try {
				getLog().d("next task to schedule: "+ G.get().getPlatform(platformId).getSubTaskForProcessor(dest.getSubTaskId()));
				addTaskToDestination(dest);
			} catch (TooMuchConcurrentTasksException e) {
				getLog().w("too many concurrent tasks! failing task.",e);
				G.get().getPlatform(platformId).getSubTaskForProcessor(dest.getSubTaskId()).fail(e);
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
