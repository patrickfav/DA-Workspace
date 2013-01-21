package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.TaskWorkManager;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.IComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ExecutionCallback;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.environment.task.thread.ExecutionRunnable;
import at.ac.tuwien.e0426099.simulator.exceptions.CantStartException;
import at.ac.tuwien.e0426099.simulator.exceptions.RunOnIllegalStateException;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class ComputationalSubTask implements IComputationalSubTask,ExecutionCallback {

	private Logger log = LogManager.getLogger(ComputationalSubTask.class.getName());
	private SubTaskId id;
	private PlatformId platformId;
	private String readAbleName;
	private SubTaskStatus status;
	private TaskType type;
	private MemoryAmount memoryDemand;
	private List<ITaskListener> listeners;
	private RawProcessingPower availableProcPower;
	private double processingHandicap;
	private ProcessingRequirements requirements;
	private TaskWorkManager taskWorkManager;
	private Exception exception;
	private Future futureRefForThread;
	private Semaphore startStopSemaphore;
	private Semaphore checkOnFinishSemaphore;

	public ComputationalSubTask(String readAbleName, MemoryAmount neededForExecution, Long maxComputationalUtilization, Long computationsNeededForFinishing) {
		id = new SubTaskId();
		this.readAbleName = readAbleName;
		setStatus(SubTaskStatus.NOT_STARTED);
		type = TaskType.PROCESSING;
		memoryDemand = neededForExecution;
		availableProcPower =new RawProcessingPower(0);
		processingHandicap=0;
		requirements = new ProcessingRequirements(new RawProcessingPower(maxComputationalUtilization),computationsNeededForFinishing);
		taskWorkManager = new TaskWorkManager(requirements);
		listeners = new ArrayList<ITaskListener>();
		startStopSemaphore = new Semaphore(0, true); //initialise with 0, so first aquire waits
		checkOnFinishSemaphore =new Semaphore(1,true);
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
	public void setParentId(UUID parentTaskId) {
		id.upadteParentId(parentTaskId);
	}

	@Override
	public void setPlatformId(PlatformId id) {
		platformId = id;
	}

	@Override
	public synchronized void pause() {
		if(status == SubTaskStatus.RUNNING) {
			setStatus(SubTaskStatus.PAUSED);
			interruptExecThread();
			aquireSempahore(startStopSemaphore,"startStopPause()");
		} else {
			logMsgWarn("Can't pause while not running");
		}
	}

	@Override
	public synchronized void run() {
        if(status == SubTaskStatus.FINISHED) {
            logMsgWarn("This seems to be a conccurrent error, nothing to worry about that much: try to run in FINISH state. The run attempt will be ignored.");
        } else {
            aquireSempahore(checkOnFinishSemaphore,"checkFinishRun()");
            if(status == SubTaskStatus.NOT_STARTED || status == SubTaskStatus.PAUSED) {
                setStatus(SubTaskStatus.RUNNING);
                futureRefForThread = G.get().getPlatform(platformId).getThreadPool().submit(new ExecutionRunnable(availableProcPower.getEstimatedTimeInMsToFinish(taskWorkManager.getComputationsLeftToDo()),this));
            } else {
                throw new RunOnIllegalStateException("Can only start running when in pause or not started, but was in state "+status+" in "+ getLogRef());
            }
            aquireSempahore(startStopSemaphore,"startStopRun()");
            releaseSempahore(checkOnFinishSemaphore,"checkFinishRun()");
        }
	}

	@Override
	public synchronized void fail(Exception e) {
		aquireSempahore(checkOnFinishSemaphore,"checkFinsihFail()");
		logMsgInfo("Task failed. ["+e.getClass().getSimpleName()+"]");
		setStatus(SubTaskStatus.SIMULATED_ERROR);
		exception=e;
		interruptExecThread();
		aquireSempahore(startStopSemaphore,"startStopFail()");
		releaseSempahore(checkOnFinishSemaphore,"checkFinsihFail()");
	}

	@Override
	public synchronized void updateAvailableProcessingPower(RawProcessingPower resources) {
		if(resources.getComputationsPerMs() > requirements.getMaxComputationalUtilization().getComputationsPerMs()) {
			availableProcPower = requirements.getMaxComputationalUtilization();
		} else {
			availableProcPower = resources;
		}
	}

	/* ***************************************************************************** CALLBACKS FROM THREAD */

	@Override
	public void onExecRun() {
		long timeToSleep= 0;
		try {
			timeToSleep = taskWorkManager.startProcessing(new Date(),availableProcPower);
		} catch (CantStartException e) {
			throw new RunOnIllegalStateException("Trying to start while still running (Status: "+status+") in task "+ getLogRef());
		}
		logMsgInfo("Start task. Estimated time to finish: " + timeToSleep+"ms");
		releaseSempahore(startStopSemaphore,"startStopExecRun");
	}

	@Override
	public void onExecFinished() {
		aquireSempahore(checkOnFinishSemaphore,"checkFinishOnExecFinsish");
		taskWorkManager.stopCurrentProcessing();
		if(taskWorkManager.getComputationsLeftToDo() <= 0) {
			logMsgInfo("Task Finished. [Net time spent: "+String.valueOf(taskWorkManager.getNetTimeSpendOnComputation())+"ms," +
					" Overall spent: "+String.valueOf(taskWorkManager.getOverallTimeSpendOnComputation())+"ms]");
			setStatus(SubTaskStatus.FINISHED);
		}

		releaseSempahore(checkOnFinishSemaphore,"checkFinishOnExecFinsish");

		if(taskWorkManager.getComputationsLeftToDo() <= 0) {
			callAllListenerFinished();
		}
	}

	@Override
	public void onExecInterrupted() {
		if(status == SubTaskStatus.PAUSED) {
			taskWorkManager.stopCurrentProcessing();
			logMsgInfo("Task paused. Time spent since last start: "+((taskWorkManager.getRecentSlice() != null) ? taskWorkManager.getRecentSlice().getActualTimeSpendOnComputation(): "null ")+"ms");
		} else {
			logMsgWarn("Task interrupted but not from our Framework, that's strange. Status: "+status);
		}
		releaseSempahore(startStopSemaphore,"startStopExecInterrupt");
	}

	@Override
	public void onExecException(Exception e) {
		taskWorkManager.stopCurrentProcessing();
		setStatus(SubTaskStatus.CONCURRENT_ERROR);
		exception=e;
		log.error(getLogRef()+": "+"Exception thrown while executing Thread",e);
		releaseSempahore(startStopSemaphore,"startStopExecException");
		callAllListenerFailed();

	}

    /* ***************************************************************************** GETTER N SETTER */

    @Override
    public void setProcessingHandicap(double percentage) {
        processingHandicap = percentage;
    }

    @Override
    public RawProcessingPower getCurrentlyAssignedProcessingPower() {
        return availableProcPower;
    }

    @Override
    public SubTaskStatus getStatus() {
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
        return getLogRef();
    }

    @Override
    @Deprecated
    public void waitForTaskToFinish() {
        if(futureRefForThread != null) {
            try {
                futureRefForThread.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.TAB+getLogRef());
        sb.append(LogUtil.TAB+" Status: "+status+", Needed Memory: "+memoryDemand+", ProcReqs: "+requirements+ LogUtil.BR);
        sb.append(LogUtil.TAB+LogUtil.TAB+"Summary: Net time spent: "+String.valueOf(taskWorkManager.getNetTimeSpendOnComputation())+"ms, Overall: "+String.valueOf(taskWorkManager.getOverallTimeSpendOnComputation())+"ms, Min: "+requirements.getMinimumExecutionTimeMs()+" ms"+ LogUtil.BR);

        if(detailed)
            for(int i=0; i< taskWorkManager.getProcessingSlices().size();i++) {
                sb.append(LogUtil.TAB+LogUtil.TAB+LogUtil.TAB+" Processingslice "+(i+1)+": "+taskWorkManager.getProcessingSlices().get(i)+ LogUtil.BR);
            }

        return sb.toString();
    }

	/* ***************************************************************************** PRIVATES */

	private synchronized void interruptExecThread() {
		logMsgVerbose("interrupt called");
		if(futureRefForThread != null) {
			if(!futureRefForThread.cancel(true)) {
				releaseSempahore(startStopSemaphore,"startStopInterrupt"); //release if it cant be canceled, since it would never get to callbacks
			}
		}
		futureRefForThread =null; //can only interrupt once
	}

	private synchronized void setStatus(SubTaskStatus t) {
		logMsgDebug("Status change from "+status+" to " + t);
		status = t;
	}

	private void callAllListenerFinished() {
		for(ITaskListener l:listeners) {
			l.onTaskFinished(id);
		}
	}
	private void callAllListenerFailed() {
		for(ITaskListener l:listeners) {
			l.onTaskFailed(id);
		}
	}

	private void aquireSempahore(Semaphore s, String msg) {
        logMsgVerbose("Start acquiring semaphore: "+msg);
        s.acquireUninterruptibly();
        logMsgVerbose("Done acquiring "+msg);
	}

	private void releaseSempahore(Semaphore s, String msg) {
		logMsgVerbose("Release semaphore: "+msg);
		s.release();
	}

    /* LOGGING */

    private void logMsgWarn(String msg) {
        log.warn(getLogRef()+": "+msg);
    }
    private void logMsgVerbose(String msg) {
        if(G.VERBOSE_LOG_MODE)
            log.debug(getLogRef()+": "+msg);
    }

    private void logMsgDebug(String msg) {
        log.debug(getLogRef()+": "+msg);
    }

    private void logMsgInfo(String msg) {
        log.info(getLogRef()+": "+msg);
    }

    private String getLogRef() {
        return "["+platformId+"|"+type+"|"+readAbleName+"/"+id.getSubTaskId().toString().substring(0,5)+"]";
    }


}
