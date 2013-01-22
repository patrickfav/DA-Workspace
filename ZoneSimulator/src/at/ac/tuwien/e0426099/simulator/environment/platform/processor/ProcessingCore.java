package at.ac.tuwien.e0426099.simulator.environment.platform.processor;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.abstracts.APauseAbleThread;
import at.ac.tuwien.e0426099.simulator.environment.platform.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.platform.memory.WorkingMemory;
import at.ac.tuwien.e0426099.simulator.environment.task.comparator.ProcPwrReqIdComparator;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingCore extends APauseAbleThread<SubTaskId> implements ITaskListener,WorkingMemory.ChangedMemoryListener {
	private Logger log = LogManager.getLogger(ProcessingCore.class.getName());

	private UUID id;
	private PlatformId platformId;
	private RawProcessingPower rawProcessingPower;
	private int maxConcurrentTasks;
	private double concurrentTaskPenaltyPercentage;
	private ProcessingUnitListener processingUnit;
	private String coreName;
	private List<SubTaskId> currentRunningTasks;

	public ProcessingCore(RawProcessingPower rawProcessingPower, int maxConcurrentTasks, double concurrentTaskPenaltyPercentage) {
		this.rawProcessingPower = rawProcessingPower;
		this.maxConcurrentTasks = maxConcurrentTasks;
		this.concurrentTaskPenaltyPercentage = concurrentTaskPenaltyPercentage;
		coreName ="Unassigned Core";
		currentRunningTasks=Collections.synchronizedList(new ArrayList<SubTaskId>());
		id=UUID.randomUUID();
	}

	public synchronized void addTask(SubTaskId subTaskId) throws TooMuchConcurrentTasksException {
		log.debug(getLogRef() + "Add task " + subTaskId);
		G.get().getPlatform(platformId).getSubTaskForProcessor(subTaskId).addTaskListener(this);//set listener for callback

		if(!acceptsNewTask()) { //just wait in queue
			throw new TooMuchConcurrentTasksException("Cannot add this task "+ G.get().getPlatform(platformId).getSubTaskForProcessor(subTaskId).getReadAbleName()+
					" to core "+id+", since the maximum of "+maxConcurrentTasks+" is reached in this core.");
		} else { //reshare processing power
			pauseAllUnfinishedTasks();
			currentRunningTasks.add(subTaskId); //add new task
			addToWorkerQueue(subTaskId);
		}
	}

	public synchronized boolean acceptsNewTask() {
		return currentRunningTasks.size() < maxConcurrentTasks;
	}

	public synchronized int getCurrentRunningTasksSize() {
		return currentRunningTasks.size();
	}

	/**
	 * Returns the load of this core
	 * @return 0.0 - 1.0 where 1.0 is 100%
	 */
	public synchronized double getLoad() {
		long sum = 0;
		for(SubTaskId t: currentRunningTasks) {
			sum += G.get().getPlatform(platformId).getSubTaskForProcessor(t).getCurrentlyAssignedProcessingPower().getComputationsPerMs();
		}
		return rawProcessingPower.getComputationsPerMs(concurrentTaskPenaltyPercentage*(currentRunningTasks.size()-1)) / (double) sum;
	}

	public synchronized ProcessingCoreInfo getInfo() {
		return new ProcessingCoreInfo(id, coreName,getLoad(),getCurrentRunningTasksSize(),maxConcurrentTasks);
	}


    public synchronized int getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

	public UUID getCoreId() {
		return id;
	}

	public synchronized void setPlatformId(PlatformId platformId) {
		this.platformId = platformId;
	}

    public synchronized void setProcessingUnitListener(ProcessingUnitListener processingUnit) {
        this.processingUnit = processingUnit;
    }

    public String getCoreName() {
        return coreName;
    }

    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }

	@Override
	public String toString() {
		return getLogRef();
	}

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.BR);
        sb.append(getLogRef()+" ProcPower: "+rawProcessingPower+", MaxConcurrentTasks: "+maxConcurrentTasks+ ", ConcTaskPenalty: "+concurrentTaskPenaltyPercentage +LogUtil.BR);
        sb.append("Current Running Tasks:" +LogUtil.BR);
        sb.append(LogUtil.emptyListText(currentRunningTasks," - no tasks -"));
        for(SubTaskId id:currentRunningTasks) {
            sb.append(G.get().getPlatform(platformId).getSubTaskForProcessor(id).getCompleteStatus(detailed)+LogUtil.BR);
        }
        return sb.toString();
    }

	/* ********************************************************************************** THREAD ABSTRACT IMPL*/

	@Override
	public  void doTheWork(SubTaskId input) {
		reBalanceTasks();
		runAllTasks();
	}

	@Override
	public  void onAllDone() {
		//do nothing
	}

	@Override
	public  boolean checkIfThereWillBeAnyWork() {
		if(!super.checkIfThereWillBeAnyWork()) {
			return !currentRunningTasks.isEmpty();
		}

		return super.checkIfThereWillBeAnyWork();
	}

	@Override
	public void pause() {
		super.pause();
		pauseAllUnfinishedTasks();
	}

	@Override
	public void resumeExec() {
		super.resumeExec();
		runAllTasks();
	}

	/* ********************************************************************************** CALLBACKS */

    @Override
    public synchronized void onTaskFinished(SubTaskId subTaskId) {
        pauseAllUnfinishedTasks();
        log.debug(getLogRef() + "Remove " + subTaskId + " from running tasks");
        currentRunningTasks.remove(subTaskId); //removed finished task
		addToWorkerQueue(subTaskId);
        processingUnit.onTaskFinished(this,subTaskId); //inform processing unit
    }

    @Override
    public synchronized void onTaskFailed(SubTaskId subTaskId) {
        pauseAllUnfinishedTasks();
        log.debug(getLogRef() + "Failed task " + subTaskId + " removed");
        currentRunningTasks.remove(subTaskId); //removed failed task
		addToWorkerQueue(subTaskId);
        processingUnit.onTaskFailed(this, subTaskId); //inform processing unit
    }


    @Override
    public synchronized void subTaskHaveAlteredMemoryAssignement(List<SubTaskId> subTaskIds) {
        boolean needsRebalancing = false;
        for(SubTaskId idOuter:subTaskIds) {
            for(SubTaskId idInner:currentRunningTasks) {
                if(idOuter.equals(idInner)) {
                    needsRebalancing = true;
                    break;
                }
            }

            if(needsRebalancing)
                break;
        }

        if(needsRebalancing)
            reBalanceTasks();

    }

	/* ********************************************************************************** PRIVATES */

	private void reBalanceTasks() {
		Collections.sort(currentRunningTasks, new ProcPwrReqIdComparator(platformId)); //sort lower demanding task first

		double currentProcPwrP = rawProcessingPower.getComputationsPerMs(concurrentTaskPenaltyPercentage*(currentRunningTasks.size()-1)); //pwr - penality for concurrent processing
		double maxProcPwrPerTask =  currentProcPwrP/currentRunningTasks.size(); //fairly shared resources

		for(int i=0; i<currentRunningTasks.size(); i++) {
			if(G.get().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs() <= (long) maxProcPwrPerTask) { //needs less procPwr as provided
				double procPwrThatCanBeSharedAgain = maxProcPwrPerTask-(double) G.get().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs();
				G.get().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).updateAvailableProcessingPower(G.get().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization());
				maxProcPwrPerTask += procPwrThatCanBeSharedAgain/(currentRunningTasks.size()-i+1); //divide upon remaining tasks
			} else {
				G.get().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).updateAvailableProcessingPower(new RawProcessingPower((long) Math.floor(maxProcPwrPerTask)));
			}
		}
	}

	private synchronized void pauseAllUnfinishedTasks() {
		for(SubTaskId t: currentRunningTasks) {
			if(G.get().getPlatform(platformId).getSubTaskForProcessor(t).getStatus() != ISubTask.SubTaskStatus.FINISHED) {
				log.debug(getLogRef()+"Pause Task "+t+".");
				G.get().getPlatform(platformId).getSubTaskForProcessor(t).pause();
				//G.get().getPlatform(platformId).getSubTaskForProcessor(t).waitForPause();
			}
		}
	}

	private synchronized void runAllTasks() {
		for(SubTaskId t: currentRunningTasks) {
			log.debug(getLogRef()+"Run Task "+t+".");
			try {
				G.get().getPlatform(platformId).getSubTaskForProcessor(t).run();
			} catch (Exception e) {
				log.error(getLogRef()+"Exception caught while trying to run all tasks",e);
			}
		}
	}

	private String getLogRef(){
		return "["+platformId+"|CPU|Core|"+coreName+"]: ";
	}
}
