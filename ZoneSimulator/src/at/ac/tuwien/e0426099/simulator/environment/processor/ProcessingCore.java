package at.ac.tuwien.e0426099.simulator.environment.processor;

import at.ac.tuwien.e0426099.simulator.environment.GodClass;
import at.ac.tuwien.e0426099.simulator.environment.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.memory.WorkingMemory;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingCoreInfo;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.ProcessingUnitListener;
import at.ac.tuwien.e0426099.simulator.environment.task.comparator.ProcPwrReqIdComparator;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.exceptions.TooMuchConcurrentTasksException;
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
public class ProcessingCore implements ITaskListener,WorkingMemory.ChangedMemoryListener {
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
		GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(subTaskId).addTaskListener(this);//set listener for callback

		if(!acceptsNewTask()) { //just wait in queue
			throw new TooMuchConcurrentTasksException("Cannot add this task "+GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(subTaskId).getReadAbleName()+
					" to core "+id+", since the maximum of "+maxConcurrentTasks+" is reached in this core.");
		} else { //reshare processing power
			pauseAllUnfinishedTasks();

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
		pauseAllUnfinishedTasks();
		log.debug(getLogRef() + "Remove " + subTaskId + " from running tasks");
		currentRunningTasks.remove(subTaskId); //removed finished task
		reBalanceTasks();
		runAllTasks();
		processingUnit.onTaskFinished(this,subTaskId); //inform processing unit
	}

	@Override
	public synchronized void onTaskFailed(SubTaskId subTaskId) {
		pauseAllUnfinishedTasks();
		log.debug(getLogRef() + "Failed task " + subTaskId + " removed");
		currentRunningTasks.remove(subTaskId); //removed finished task
		reBalanceTasks();
		runAllTasks();
		processingUnit.onTaskFailed(this, subTaskId); //inform processing unit
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
			sum += GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(t).getCurrentlyAssignedProcessingPower().getComputationsPerMs();
		}
		return rawProcessingPower.getComputationsPerMs(concurrentTaskPenaltyPercentage*(currentRunningTasks.size()-1)) / (double) sum;
	}

	public synchronized ProcessingCoreInfo getInfo() {
		return new ProcessingCoreInfo(id, coreName,getLoad(),getCurrentRunningTasksSize(),maxConcurrentTasks);
	}

	public UUID getCoreId() {
		return id;
	}

	public void setPlatformId(PlatformId platformId) {
		this.platformId = platformId;
	}
	/* ********************************************************************************** PRIVATES */

	private void reBalanceTasks() {
		Collections.sort(currentRunningTasks, new ProcPwrReqIdComparator(platformId)); //sort lower demanding task first

		double currentProcPwrP = rawProcessingPower.getComputationsPerMs(concurrentTaskPenaltyPercentage*(currentRunningTasks.size()-1)); //pwr - penality for concurrent processing
		double maxProcPwrPerTask =  currentProcPwrP/currentRunningTasks.size(); //fairly shared resources

		for(int i=0; i<currentRunningTasks.size(); i++) {
			if(GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs() <= (long) maxProcPwrPerTask) { //needs less procPwr as provided
				double procPwrThatCanBeSharedAgain = maxProcPwrPerTask-(double) GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs();
				GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).updateAvailableProcessingPower(GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).getProcessingRequirements().getMaxComputationalUtilization());
				maxProcPwrPerTask += procPwrThatCanBeSharedAgain/(currentRunningTasks.size()-i+1); //divide upon remaining tasks
			} else {
				GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(currentRunningTasks.get(i)).updateAvailableProcessingPower(new RawProcessingPower((long) Math.floor(maxProcPwrPerTask)));
			}
		}
	}

	private void pauseAllUnfinishedTasks() {
		for(SubTaskId t: currentRunningTasks) {
			if(GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(t).getStatus() != ISubTask.SubTaskStatus.FINISHED) {
				log.debug(getLogRef()+"Pause Task "+t+".");
				GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(t).pause();
				//GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(t).waitForPause();
			}
		}
	}

	private void runAllTasks() {
		for(SubTaskId t: currentRunningTasks) {
			log.debug(getLogRef()+"Run Task "+t+".");
			try {
				GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(t).run();
				//GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(t).waitForStartedRunning();
			} catch (Exception e) {
				log.error(getLogRef()+"Exception caught while trying to run all tasks",e);
			}
		}
	}

	private String getLogRef(){
		return "["+platformId+"|CPU|Core|"+coreName+"]: ";
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

	public String getCoreName() {
		return coreName;
	}

	public void setCoreName(String coreName) {
		this.coreName = coreName;
	}

	@Override
	public String toString() {
		return coreName;
	}
}
