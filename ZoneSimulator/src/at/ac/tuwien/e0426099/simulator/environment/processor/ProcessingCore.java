package at.ac.tuwien.e0426099.simulator.environment.processor;

import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.task.IRunnableTask;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.environment.task.comparator.ProcPwrReqComparator;

import java.util.*;

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

	public synchronized void addTask(IRunnableTask task) throws TooMuchConcurrentTasksException {
		task.setTaskListener(this);//set listener for callback

		if(!acceptsNewTask()) { //just wait in queue
			throw new TooMuchConcurrentTasksException("Cannot add this task "+task.getReadAbleName()+
					" to core "+id+", since the maximum of "+maxConcurrentTasks+" is reached in this core.");
		} else { //reshare processing power
			pauseAllRunningTasks();

			currentRunningTasks.add(task); //add new task

			reBalanceTasks();

			runAllTasks();
		}
	}

	public synchronized boolean acceptsNewTask() {
		return currentRunningTasks.size() < maxConcurrentTasks;
	}

	@Override
	public synchronized void onTaskFinished(IRunnableTask task) {
		pauseAllRunningTasks();

		currentRunningTasks.remove(task); //removed finished task

		reBalanceTasks();

		runAllTasks();

		processingUnit.onTaskFinished(this,task); //inform processing unit
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
		for(IRunnableTask t: currentRunningTasks) {
			sum += t.getCurrentlyAssignedProcessingPower();
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
			t.pause();
		}
	}

	private void runAllTasks() {
		for(IRunnableTask t: currentRunningTasks) {
			t.run();
		}
	}

	/* ********************************************************************************** INNER CLASSES */

	public interface ProcessingUnitListener {
		public void onTaskFinished(ProcessingCore c,IRunnableTask t);
	}

}
