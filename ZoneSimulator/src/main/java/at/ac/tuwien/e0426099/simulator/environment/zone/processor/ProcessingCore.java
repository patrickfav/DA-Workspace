package at.ac.tuwien.e0426099.simulator.environment.zone.processor;

import at.ac.tuwien.e0426099.simulator.environment.Env;
import at.ac.tuwien.e0426099.simulator.environment.abstracts.APauseAbleThread;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.environment.zone.memory.WorkingMemory;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.ActionWrapper;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.helper.comparators.ProcPwrReqIdComparator;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;
import at.ac.tuwien.e0426099.simulator.helper.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A cores is responsibly for running the subtask. A core can execute multiple subtasks at the same time (configurable)
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingCore extends APauseAbleThread<ActionWrapper> implements ITaskListener,WorkingMemory.ChangedMemoryListener {
	
	private UUID id;
	private ZoneId zoneId;
	private RawProcessingPower rawProcessingPower;
	private int maxConcurrentTasks;
	private double concurrentTaskPenaltyPercentage;
	private ProcessingUnitListener processingUnit;
	private String coreName;
	private List<SubTaskId> currentRunningTasks;
	private boolean executionFactorChanged;

	public ProcessingCore(RawProcessingPower rawProcessingPower, int maxConcurrentTasks, double concurrentTaskPenaltyPercentage) {
		this.rawProcessingPower = rawProcessingPower;
		this.maxConcurrentTasks = maxConcurrentTasks;
		rawProcessingPower.setPenalty(concurrentTaskPenaltyPercentage);
		executionFactorChanged =false;
		coreName ="Unassigned Core";
		currentRunningTasks=Collections.synchronizedList(new ArrayList<SubTaskId>());
		id=UUID.randomUUID();
		getLog().refreshData();
	}

	public void addTask(SubTaskId subTaskId) throws TooMuchConcurrentTasksException {
		getLog().d("Add task " + subTaskId);
		Env.get().getZone(zoneId).getSubTaskForProcessor(subTaskId).addTaskListener(this);//set listener for callback

		if(!acceptsNewTask()) { //just wait in queue
			throw new TooMuchConcurrentTasksException("Cannot add this task "+ Env.get().getZone(zoneId).getSubTaskForProcessor(subTaskId).getReadAbleName()+
					" to core "+id+", since the maximum of "+maxConcurrentTasks+" is reached in this core.");
		} else { //reshare processing power
			addToWorkerQueue(new ActionWrapper(subTaskId, ActionWrapper.ActionType.ADD,this));
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
	public double getLoad() {
		long sum = 0;
		for(SubTaskId t: currentRunningTasks) {
			sum += Env.get().getZone(zoneId).getSubTaskForProcessor(t).getCurrentlyAssignedProcessingPower().getComputationsPerMs();
		}
		if(sum == 0)
			return 0;

		return Math.min(1,(double) sum / rawProcessingPower.getComputationsPerMsForPenalty(currentRunningTasks.size()));
	}

	public ProcessingCoreInfo getInfo() {
		return new ProcessingCoreInfo(id, coreName,getLoad(),getCurrentRunningTasksSize(),maxConcurrentTasks);
	}


    public int getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

	@Override
	public UUID getCoreId() {
		return id;
	}

	public void setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
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
		return "["+ zoneId +"|CPU|Core|"+coreName+"]";
	}

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.BR);
        sb.append(toString()+" ProcPower: "+rawProcessingPower+", MaxConcurrentTasks: "+maxConcurrentTasks+ ", ConcTaskPenalty: "+concurrentTaskPenaltyPercentage +LogUtil.BR);
        sb.append("Current Running Tasks:" +LogUtil.BR);
        sb.append(LogUtil.emptyListText(currentRunningTasks," - no tasks -"));
        for(SubTaskId id:currentRunningTasks) {
            sb.append(Env.get().getZone(zoneId).getSubTaskForProcessor(id).getCompleteStatus(detailed)+LogUtil.BR);
        }
        return sb.toString();
    }

	/* ********************************************************************************** THREAD ABSTRACT IMPL*/

	@Override
	public void doTheWork(ActionWrapper input) {
		getWorkLock().lock();
		getLog().d("start next interation (doWork)");
		pauseAllUnfinishedTasks();
		if(input != null) {
			if(input.getActionType().equals(ActionWrapper.ActionType.ADD)) {
				getLog().d("Add " + input.getSubTaskId() + " to running tasks");
				currentRunningTasks.add(input.getSubTaskId()); //add new task
			} else if(input.getActionType().equals(ActionWrapper.ActionType.FINISH)) {
				getLog().d("Finish: Remove " + input.getSubTaskId() + " from running tasks");
				currentRunningTasks.remove(input.getSubTaskId()); //remove
				processingUnit.onTaskFinished(this,input.getSubTaskId()); //inform processing unit
			} else if(input.getActionType().equals(ActionWrapper.ActionType.FAIL)) {
				getLog().d("Fail: Remove " + input.getSubTaskId() + " from running tasks");
				currentRunningTasks.remove(input.getSubTaskId()); //remove
				processingUnit.onTaskFailed(this,input.getSubTaskId()); //inform processing unit
			}
		}

		reBalanceTasks();
		runAllTasks();
		getWorkLock().unlock();
	}

	@Override
	public void onAllDone() {
		//do nothing
	}

	@Override
	public boolean checkIfThereWillBeAnyWork() {
		if(!super.checkIfThereWillBeAnyWork()) {
			return !currentRunningTasks.isEmpty();
		}
		return super.checkIfThereWillBeAnyWork();
	}

	@Override
	public void pause() {
		pauseAllUnfinishedTasks();
		super.pause();
	}

	@Override
	public void resumeExec() {
		super.resumeExec();
		runAllTasks();
	}

	@Override
	public void setExecutionFactor(double executionFactor) {
		super.setExecutionFactor(executionFactor);
		executionFactorChanged =true;
	}

	/* ********************************************************************************** CALLBACKS */

    @Override
    public void onTaskFinished(SubTaskId subTaskId) {
		getLog().d("Task onFinish called by " + subTaskId);
		addToWorkerQueue(new ActionWrapper(subTaskId, ActionWrapper.ActionType.FINISH,this));
    }

    @Override
    public void onTaskFailed(SubTaskId subTaskId) {
		getLog().d("Task onFail called by " + subTaskId);
		addToWorkerQueue(new ActionWrapper(subTaskId, ActionWrapper.ActionType.FAIL,this));
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
		double currentProcPwrP = rawProcessingPower.getComputationsPerMsForPenalty(currentRunningTasks.size()); //pwr - penality for concurrent processing
		double maxProcPwrPerTask =  currentProcPwrP/Math.max(1, currentRunningTasks.size()); //fairly shared resources

		getLog().d("Rebalance tasks. CurrentPower with penality "+concurrentTaskPenaltyPercentage+" is "+currentProcPwrP+" and will be divided upon "+currentRunningTasks.size()+" running tasks, so maxPowerIs "+maxProcPwrPerTask+" per task");

		Collections.sort(currentRunningTasks, new ProcPwrReqIdComparator(zoneId)); //sort lower demanding task first

		for(int i=0; i<currentRunningTasks.size(); i++) {
			if(Env.get().getZone(zoneId).getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs() <= (long) maxProcPwrPerTask) { //needs less procPwr as provided
				double procPwrThatCanBeSharedAgain = maxProcPwrPerTask-(double) Env.get().getZone(zoneId).getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs();
				Env.get().getZone(zoneId).getSubTaskForProcessor(currentRunningTasks.get(i)).updateAvailableProcessingPower(Env.get().getZone(zoneId).getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization());
				maxProcPwrPerTask += procPwrThatCanBeSharedAgain/(currentRunningTasks.size()-i+1); //divide upon remaining tasks
			} else {
				Env.get().getZone(zoneId).getSubTaskForProcessor(currentRunningTasks.get(i)).updateAvailableProcessingPower(new RawProcessingPower((long) Math.floor(maxProcPwrPerTask)));
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
			if(Env.get().getZone(zoneId).getSubTaskForProcessor(t).getStatus() != ISubTask.SubTaskStatus.FINISHED) {
				getLog().v("Pause Task "+t+".");
				Env.get().getZone(zoneId).getSubTaskForProcessor(t).pause();
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
				if(executionFactorChanged) {
					Env.get().getZone(zoneId).getSubTaskForProcessor(t).setExecutionFactor(getExecutionFactor());
					executionFactorChanged=false;
				}
				Env.get().getZone(zoneId).getSubTaskForProcessor(t).run();
			} catch (Exception e) {
				Env.get().getZone(zoneId).getSubTaskForProcessor(t).fail(e);
				getLog().e("Exception caught while trying to run all tasks",e);
			}
		}
	}

	/* **************************************************************************** INNER CLASS */


}
