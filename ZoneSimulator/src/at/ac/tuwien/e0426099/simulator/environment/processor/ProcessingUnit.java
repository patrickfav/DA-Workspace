package at.ac.tuwien.e0426099.simulator.environment.processor;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.CoreDestination;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.TaskManagementMemoryListener;
import at.ac.tuwien.e0426099.simulator.environment.processor.scheduler.IScheduler;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingUnit extends Thread implements ProcessingUnitListener {
	private Logger log = LogManager.getLogger(ProcessingUnit.class.getName());

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
	}

	public synchronized void addTask(SubTaskId subTaskId) {
		log.debug(getLogRef()+"adding subtask to cpu "+subTaskId);
		memoryCallBack.onSubTaskAdded(subTaskId);
		scheduler.addToQueue(subTaskId);
		scheduleTasks();
	}

	public synchronized void setPlatformId(PlatformId platformId) {
		this.platformId = platformId;
		this.memoryCallBack.setPlatformId(platformId);
		for(int i=0;i<cores.size();i++) {
			cores.get(i).setPlatformId(platformId);
		}
	}

	public synchronized void setPlatformCallBack(ProcessingUnitListener platformCallBack) {
		this.platformCallBack = platformCallBack;
	}

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.BR+LogUtil.h3("CPU "+getLogRef()));
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


    /* ********************************************************************************** CALLBACKS */

    @Override
    public synchronized void onTaskFinished(ProcessingCore c, SubTaskId subTaskId) {
        log.debug(getLogRef()+"task finished in "+c.getCoreName()+", spreading the word: "+subTaskId);
        memoryCallBack.onSubTaskAdded(subTaskId);
        finnishedSubTasks.add(subTaskId);
        scheduleTasks();
        G.get().getWaitForTasksToFinish().release();
        platformCallBack.onTaskFinished(c,subTaskId);
    }

    @Override
    public synchronized void onTaskFailed(ProcessingCore c, SubTaskId subTaskId) {
        failedSubTasks.add(subTaskId);
        scheduleTasks();
        G.get().getWaitForTasksToFinish().release();
        platformCallBack.onTaskFailed(c, subTaskId);
    }

	/* ********************************************************************************** PRIVATES */

	private synchronized void scheduleTasks() {
		log.debug(getLogRef()+"scheduling tasks between cores");
		CoreDestination dest;
		List<ProcessingCoreInfo> coreInfos = getAllInfos();

		while((dest=scheduler.getNext(coreInfos)) != null) {
			try {
				log.debug(getLogRef()+"next task to schedule: "+ G.get().getPlatform(platformId).getSubTaskForProcessor(dest.getSubTaskId()).getReadAbleName());
				addTaskToDestination(dest);
			} catch (TooMuchConcurrentTasksException e) {
				log.debug(getLogRef()+"too much concurrent tasks! failing task.");
				G.get().getPlatform(platformId).getSubTaskForProcessor(dest.getSubTaskId()).fail(e);
				failedSubTasks.add(dest.getSubTaskId());
			}
		}
	}

	private synchronized void addTaskToDestination(CoreDestination dest) throws TooMuchConcurrentTasksException {
		for(ProcessingCore core: cores) {
			if(core.getCoreId().equals(dest.getCoreId())) {
				core.addTask(dest.getSubTaskId());
			}
		}
	}

	private synchronized List<ProcessingCoreInfo> getAllInfos() {
		List<ProcessingCoreInfo> list = new ArrayList<ProcessingCoreInfo>();
		for(ProcessingCore core: cores) {
			list.add(core.getInfo());
		}
		return list;
	}

	private String getLogRef(){
		return "["+platformId+"|CPU]: ";
	}
}
