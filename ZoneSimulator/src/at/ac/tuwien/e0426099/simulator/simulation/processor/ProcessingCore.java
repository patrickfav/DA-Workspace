package at.ac.tuwien.e0426099.simulator.simulation.processor;

import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasks;
import at.ac.tuwien.e0426099.simulator.simulation.task.IRunnableTask;
import at.ac.tuwien.e0426099.simulator.simulation.task.ITaskListener;
import at.ac.tuwien.e0426099.simulator.simulation.task.comparator.ProcPwrReqComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingCore implements ITaskListener{

	private UUID id;
	private RawProcessingPower rawProcessingPower;
	private int maxConcurrentTasks;
	private double concurrentTaskPenaltyPercentage;
	private ProcessingUnitListener processingUnit;

	private List<IRunnableTask> currentRunningTasks;


	public ProcessingCore(RawProcessingPower rawProcessingPower, int maxConcurrentTasks, double concurrentTaskPenaltyPercentage, ProcessingUnitListener processingUnit) {
		this.rawProcessingPower = rawProcessingPower;
		this.maxConcurrentTasks = maxConcurrentTasks;
		this.concurrentTaskPenaltyPercentage = concurrentTaskPenaltyPercentage;
		this.processingUnit=processingUnit;

		currentRunningTasks=new ArrayList<IRunnableTask>();
		id=UUID.randomUUID();
	}

	public synchronized void addTask(IRunnableTask task) throws TooMuchConcurrentTasks {
		task.setTaskListener(this);//set listener for callback

		if(!acceptsNewTask()) { //just wait in queue
			throw new TooMuchConcurrentTasks("Cannot add this task "+task.getReadAbleName()+", since the maximum of "+maxConcurrentTasks+" is reached in this core.");
		} else { //reshare processing power
			pauseAllRunningTasks();

			currentRunningTasks.add(task); //add new task

			rebalanceTasks();

			runAllTasks();
		}
	}

	public synchronized boolean acceptsNewTask() {
		return currentRunningTasks.size() < maxConcurrentTasks;
	}

	@Override
	public synchronized void onTaskFinished(IRunnableTask task) {
		pauseAllRunningTasks();

		currentRunningTasks.remove(task);

		rebalanceTasks();

		runAllTasks();
	}
	/* ********************************************************************************** PRIVATES */

	private void rebalanceTasks() {
		Collections.sort(currentRunningTasks, new ProcPwrReqComparator()); //sort lower demanding task first

		double currentProcPwrP = rawProcessingPower.getComputationsPerMs(concurrentTaskPenaltyPercentage*(currentRunningTasks.size()-1)); //pwr - penality for concurrent processing
		double maxProcPwrPerTask =  currentProcPwrP/currentRunningTasks.size(); //fairly shared resources

		for(int i=0; i<currentRunningTasks.size(); i++) {
			if(currentRunningTasks.get(i).getProcessingRequirements().getMaxComputationalUtilization() <= (long) maxProcPwrPerTask) { //needs less procPwr as provided
				double procPwrThatCanBeSharedAgain = maxProcPwrPerTask-(double) currentRunningTasks.get(i).getProcessingRequirements().getMaxComputationalUtilization();
				currentRunningTasks.get(i).updateProcessingResources(currentRunningTasks.get(i).getProcessingRequirements().getMaxComputationalUtilization());
				maxProcPwrPerTask += procPwrThatCanBeSharedAgain/(i+1 - currentRunningTasks.size()); //divide upon remaining tasks
			} else {
				currentRunningTasks.get(i).updateProcessingResources((long) Math.floor(maxProcPwrPerTask));
			}
		}
	}

	private void pauseAllRunningTasks() {
		for(IRunnableTask t: currentRunningTasks) {
			t.pauseProcessing();
		}
	}

	private void runAllTasks() {
		for(IRunnableTask t: currentRunningTasks) {
			t.run();
		}
	}

	/* ********************************************************************************** INTERFACE */

	public interface ProcessingUnitListener {
		public void onTaskFinished(ProcessingCore c,IRunnableTask t);
	}

}
