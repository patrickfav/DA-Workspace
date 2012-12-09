package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.TaskWorkManager;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.IComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.environment.task.thread.ExecutionRunnable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class ComputationalSubTask implements IComputationalSubTask,ExecutionRunnable.ExecutionCallback {
	private Logger log = LogManager.getLogger(ComputationalSubTask.class.getName());

	private SubTaskId id;
	private String readAbleName;
	private TaskStatus status;
	private TaskType type;
	private MemoryAmount memoryDemand;
	private List<ITaskListener> listeners;
	private RawProcessingPower availableProcPower;
	private double processingHandicap;
	private ProcessingRequirements requirements;
	private TaskWorkManager taskWorkManager;
	private Exception exception;
	private Thread executionThread;

	public ComputationalSubTask(UUID parentTaskId, String readAbleName, MemoryAmount neededForExecution, Long maxComputationalUtilization, Long computationsNeededForFinishing) {
		id = new SubTaskId(parentTaskId,UUID.randomUUID());
		this.readAbleName = readAbleName;
		setStatus(TaskStatus.NOT_STARTED);
		type = TaskType.PROCESSING;
		memoryDemand = neededForExecution;
		availableProcPower =new RawProcessingPower(0);
		processingHandicap=0;
		requirements = new ProcessingRequirements(new RawProcessingPower(maxComputationalUtilization),computationsNeededForFinishing);
		taskWorkManager = new TaskWorkManager(requirements);
		listeners = new ArrayList<ITaskListener>();

		logMsgInfo("Task created [Type:"+type+", MinExecTime: "+requirements.getMinimumExecutionTimeMs()+"ms, MemDemand: "+memoryDemand+"]");
	}

	@Override
	public SubTaskId getSubTaskId() {
		return id;
	}

	@Override
	public String getReadAbleName() {
		return readAbleName;
	}

	@Override
	public void pause() {
		if(status == TaskStatus.RUNNING) {
			setStatus(TaskStatus.PAUSED);
			interruptExecThread();
		} else {
			logMsgWarn("Can't pause while not running");
		}
	}

	@Override
	public void run() {
		if(status == TaskStatus.NOT_STARTED || status == TaskStatus.PAUSED) {
			long timeToSleep=taskWorkManager.startProcessing(new Date(),availableProcPower);
			logMsgInfo("Start task. Estimated time to finish: " + timeToSleep+"ms");
			setStatus(TaskStatus.RUNNING);
			executionThread = new Thread(new ExecutionRunnable(timeToSleep,this));
			executionThread.start();
		} else {
			throw new RuntimeException("Can only start running when in pause or not started");
		}
	}

	@Override
	public void fail(Exception e) {
		logMsgInfo("Task failed");
		setStatus(TaskStatus.ERROR);
		exception=e;
		interruptExecThread();
	}

	@Override
	public void updateAvailableProcessingPower(RawProcessingPower resources) {
		if(resources.getComputationsPerMs() > requirements.getMaxComputationalUtilization().getComputationsPerMs()) {
			availableProcPower = requirements.getMaxComputationalUtilization();
		} else {
			availableProcPower = resources;
		}
	}

	@Override
	public void setProcessingHandicap(double percentage) {
		processingHandicap = percentage;
	}

	@Override
	public RawProcessingPower getCurrentlyAssignedProcessingPower() {
		return availableProcPower;
	}

	@Override
	public TaskStatus getStatus() {
		return status;
	}

	@Override
	public TaskType getTaskType() {
		return type;
	}

	@Override
	public ProcessingRequirements getProcessingRequirements() {
		return requirements;
	}

	@Override
	public MemoryAmount getMemoryDemand() {
		return memoryDemand;
	}

	@Override
	public void addTaskListener(ITaskListener listener) {
		listeners.add(listener);
	}


	@Override
	public String toString() {
		return getFullReadableID();
	}

	public void waitForThreadToFinish() {
		if(executionThread != null) {
			try {
				executionThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/* ***************************************************************************** CALLBACKS FROM THREAD */

	@Override
	public void onExecFinished() {
		taskWorkManager.stopCurrentProcessing();

		if(taskWorkManager.getComputationsLeftToDo() <= 0) {
			logMsgInfo("Task Finished. [Net time spent: "+String.valueOf(taskWorkManager.getNetTimeSpendOnComputation())+"ms," +
					" Overall spent: "+String.valueOf(taskWorkManager.getOverallTimeSpendOnComputation())+"ms]");
			setStatus(TaskStatus.FINISHED);

			callAllListenerFinished();			}
	}

	@Override
	public void onExecInterrupted() {
		taskWorkManager.stopCurrentProcessing();
		logMsgDebug("interrupted");
		if(status == TaskStatus.PAUSED) {
			logMsgInfo("Task paused. Time spent since last start: "+((taskWorkManager.getRecentSlice() != null) ? taskWorkManager.getRecentSlice().getActualTimeSpendOnComputation(): "null ")+"ms");
		}
	}

	@Override
	public void onExecException(Exception e) {
		taskWorkManager.stopCurrentProcessing();
		setStatus(TaskStatus.ERROR);
		log.error(e);
	}
	/* ***************************************************************************** PRIVATES */

	private void interruptExecThread() {
		if(executionThread != null) {
			executionThread.interrupt();
		}
	}

	private synchronized void setStatus(TaskStatus t) {
		logMsgDebug("Status: " + t);
		status = t;
	}

	private void logMsgWarn(String msg) {
		log.warn(getFullReadableID()+": "+msg);
	}

	private void logMsgDebug(String msg) {
		log.debug(getFullReadableID()+": "+msg);
	}

	private void logMsgInfo(String msg) {
		log.info(getFullReadableID()+": "+msg);
	}

	private String getFullReadableID() {
		return readAbleName+" ("+id.getSubTaskId().toString().substring(0,5)+"...)";
	}

	private void callAllListenerFinished() {
		for(ITaskListener l:listeners) {
			l.onTaskFinished(id);
		}
	}
}
