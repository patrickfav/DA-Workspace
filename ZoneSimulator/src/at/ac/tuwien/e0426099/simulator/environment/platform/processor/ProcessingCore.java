package at.ac.tuwien.e0426099.simulator.environment.platform.processor;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.abstracts.APauseAbleThread;
import at.ac.tuwien.e0426099.simulator.environment.platform.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.platform.memory.WorkingMemory;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.task.comparator.ProcPwrReqIdComparator;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingCore extends APauseAbleThread<ProcessingCore.CoreAction> implements ITaskListener,WorkingMemory.ChangedMemoryListener {
	
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
		getLog().refreshData();
	}

	public void addTask(SubTaskId subTaskId) throws TooMuchConcurrentTasksException {
		getLog().d("Add task " + subTaskId);
		G.get().getPlatform(platformId).getSubTaskForProcessor(subTaskId).addTaskListener(this);//set listener for callback

		if(!acceptsNewTask()) { //just wait in queue
			throw new TooMuchConcurrentTasksException("Cannot add this task "+ G.get().getPlatform(platformId).getSubTaskForProcessor(subTaskId).getReadAbleName()+
					" to core "+id+", since the maximum of "+maxConcurrentTasks+" is reached in this core.");
		} else { //reshare processing power
			addToWorkerQueue(new CoreAction(subTaskId, CoreAction.ActionType.ADD));
		}
	}

	public boolean acceptsNewTask() {
		return currentRunningTasks.size() < maxConcurrentTasks;
	}

	public int getCurrentRunningTasksSize() {
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
		if(sum == 0)
			return 0;

		return Math.min(1,(double) sum / rawProcessingPower.getComputationsPerMsForPenalty(concurrentTaskPenaltyPercentage * (currentRunningTasks.size() - 1)));
	}

	public synchronized ProcessingCoreInfo getInfo() {
		return new ProcessingCoreInfo(id, coreName,getLoad(),getCurrentRunningTasksSize(),maxConcurrentTasks);
	}


    public int getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

	public UUID getCoreId() {
		return id;
	}

	public void setPlatformId(PlatformId platformId) {
		this.platformId = platformId;
		getLog().refreshData();
	}

    public void setProcessingUnitListener(ProcessingUnitListener processingUnit) {
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
		return "["+platformId+"|CPU|Core|"+coreName+"]";
	}

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.BR);
        sb.append(toString()+" ProcPower: "+rawProcessingPower+", MaxConcurrentTasks: "+maxConcurrentTasks+ ", ConcTaskPenalty: "+concurrentTaskPenaltyPercentage +LogUtil.BR);
        sb.append("Current Running Tasks:" +LogUtil.BR);
        sb.append(LogUtil.emptyListText(currentRunningTasks," - no tasks -"));
        for(SubTaskId id:currentRunningTasks) {
            sb.append(G.get().getPlatform(platformId).getSubTaskForProcessor(id).getCompleteStatus(detailed)+LogUtil.BR);
        }
        return sb.toString();
    }

	/* ********************************************************************************** THREAD ABSTRACT IMPL*/

	@Override
	public synchronized void doTheWork(CoreAction input) {
		getLog().d("start next interation (doWork)");
		pauseAllUnfinishedTasks();
		if(input.getActionType().equals(CoreAction.ActionType.ADD)) {
			getLog().d("Add " + input.getSubTaskId() + " to running tasks");
			currentRunningTasks.add(input.getSubTaskId()); //add new task
		} else {
			getLog().d("Remove " + input.getSubTaskId() + " from running tasks");
			currentRunningTasks.remove(input.getSubTaskId()); //add new task
		}
		reBalanceTasks();
		runAllTasks();
	}

	@Override
	public void onAllDone() {
		//do nothing
	}

	@Override
	public synchronized boolean checkIfThereWillBeAnyWork() {
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
    public void onTaskFinished(SubTaskId subTaskId) {
		getLog().d("Task onFinish called by " + subTaskId);
		addToWorkerQueue(new CoreAction(subTaskId, CoreAction.ActionType.REMOVE));
        processingUnit.onTaskFinished(this,subTaskId); //inform processing unit
    }

    @Override
    public void onTaskFailed(SubTaskId subTaskId) {
		addToWorkerQueue(new CoreAction(subTaskId, CoreAction.ActionType.REMOVE));
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
		getLog().d("Rebalance tasks.");
		Collections.sort(currentRunningTasks, new ProcPwrReqIdComparator(platformId)); //sort lower demanding task first

		double currentProcPwrP = rawProcessingPower.getComputationsPerMsForPenalty(concurrentTaskPenaltyPercentage * (currentRunningTasks.size() - 1)); //pwr - penality for concurrent processing
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

	private void pauseAllUnfinishedTasks() {
		getLog().d("Pause all tasks.");

		if(currentRunningTasks.isEmpty()) {
			getLog().d("No tasks to pause.");
			return;
		}

		for(SubTaskId t: currentRunningTasks) {
			if(G.get().getPlatform(platformId).getSubTaskForProcessor(t).getStatus() != ISubTask.SubTaskStatus.FINISHED) {
				getLog().v("Pause Task "+t+".");
				G.get().getPlatform(platformId).getSubTaskForProcessor(t).pause();
			}
		}
	}

	private void runAllTasks() {
		getLog().d("Run all tasks.");

		if(currentRunningTasks.isEmpty()) {
			getLog().d("No tasks to run.");
			return;
		}

		for(SubTaskId t: currentRunningTasks) {
			getLog().v("Run Task "+t+".");
			try {
				G.get().getPlatform(platformId).getSubTaskForProcessor(t).run();
			} catch (Exception e) {
				getLog().e("Exception caught while trying to run all tasks",e);
			}
		}
	}

	/* **************************************************************************** INNER CLASS */

	public static class CoreAction {
		public enum ActionType {ADD,REMOVE}
		private SubTaskId subTaskId;
		private ActionType actionType;

		public CoreAction(SubTaskId subTask, ActionType actionType) {
			this.subTaskId = subTask;
			this.actionType = actionType;
		}

		public SubTaskId getSubTaskId() {
			return subTaskId;
		}

		public ActionType getActionType() {
			return actionType;
		}
	}
}
