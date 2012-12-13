package at.ac.tuwien.e0426099.simulator.environment.processor;

import at.ac.tuwien.e0426099.simulator.environment.Platform;
import at.ac.tuwien.e0426099.simulator.environment.memory.WorkingMemory;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.task.comparator.ProcPwrReqIdComparator;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingCore implements ITaskListener,WorkingMemory.ChangedMemoryListener {

	private UUID id;
	private RawProcessingPower rawProcessingPower;
	private int maxConcurrentTasks;
	private double concurrentTaskPenaltyPercentage;
	private ProcessingUnitListener processingUnit;

	private List<SubTaskId> currentRunningTasks;


	public ProcessingCore(RawProcessingPower rawProcessingPower, int maxConcurrentTasks, double concurrentTaskPenaltyPercentage) {
		this.rawProcessingPower = rawProcessingPower;
		this.maxConcurrentTasks = maxConcurrentTasks;
		this.concurrentTaskPenaltyPercentage = concurrentTaskPenaltyPercentage;

		currentRunningTasks=new ArrayList<SubTaskId>();
		id=UUID.randomUUID();
	}

	public synchronized void addTask(SubTaskId subTaskId) throws TooMuchConcurrentTasksException {
		Platform.instance().getSubTaskForProcessor(subTaskId).addTaskListener(this);//set listener for callback

		if(!acceptsNewTask()) { //just wait in queue
			throw new TooMuchConcurrentTasksException("Cannot add this task "+Platform.instance().getSubTaskForProcessor(subTaskId).getReadAbleName()+
					" to core "+id+", since the maximum of "+maxConcurrentTasks+" is reached in this core.");
		} else { //reshare processing power
			pauseAllRunningTasks();

			currentRunningTasks.add(subTaskId); //add new task

			reBalanceTasks();

			runAllTasks();
		}
	}

	public synchronized boolean acceptsNewTask() {
		return currentRunningTasks.size() < maxConcurrentTasks;
	}

	@Override
	public synchronized void onTaskFinished(SubTaskId subTaskId) {
		pauseAllRunningTasks();

		currentRunningTasks.remove(subTaskId); //removed finished task

		reBalanceTasks();

		runAllTasks();

		processingUnit.onTaskFinished(this,subTaskId); //inform processing unit
	}

	public int getMaxConcurrentTasks() {
		return maxConcurrentTasks;
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
			sum += Platform.instance().getSubTaskForProcessor(t).getCurrentlyAssignedProcessingPower().getComputationsPerMs();
		}
		return rawProcessingPower.getComputationsPerMs(concurrentTaskPenaltyPercentage*(currentRunningTasks.size()-1)) / (double) sum;
	}

	public synchronized ProcessingCoreInfo getInfo() {
		return new ProcessingCoreInfo(id,getLoad(),getCurrentRunningTasksSize(),maxConcurrentTasks);
	}

	public UUID getId() {
		return id;
	}

	/* ********************************************************************************** PRIVATES */

	private void reBalanceTasks() {
		Collections.sort(currentRunningTasks, new ProcPwrReqIdComparator()); //sort lower demanding task first

		double currentProcPwrP = rawProcessingPower.getComputationsPerMs(concurrentTaskPenaltyPercentage*(currentRunningTasks.size()-1)); //pwr - penality for concurrent processing
		double maxProcPwrPerTask =  currentProcPwrP/currentRunningTasks.size(); //fairly shared resources

		for(int i=0; i<currentRunningTasks.size(); i++) {
			if(Platform.instance().getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs() <= (long) maxProcPwrPerTask) { //needs less procPwr as provided
				double procPwrThatCanBeSharedAgain = maxProcPwrPerTask-(double) Platform.instance().getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs();
				Platform.instance().getSubTaskForProcessor(currentRunningTasks.get(i)).updateAvailableProcessingPower(Platform.instance().getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization());
				maxProcPwrPerTask += procPwrThatCanBeSharedAgain/(currentRunningTasks.size()-i+1); //divide upon remaining tasks
			} else {
				Platform.instance().getSubTaskForProcessor(currentRunningTasks.get(i)).updateAvailableProcessingPower(new RawProcessingPower((long) Math.floor(maxProcPwrPerTask)));
			}
		}
	}

	private void pauseAllRunningTasks() {
		for(SubTaskId t: currentRunningTasks) {
			Platform.instance().getSubTaskForProcessor(t).pause();
		}
	}

	private void runAllTasks() {
		for(SubTaskId t: currentRunningTasks) {
			Platform.instance().getSubTaskForProcessor(t).run();
		}
	}

	@Override
	public void subTaskHaveAlteredMemoryAssignement(List<SubTaskId> subTaskIds) {
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

	public void setProcessingUnitListener(ProcessingUnitListener processingUnit) {
		this.processingUnit = processingUnit;
	}
}
