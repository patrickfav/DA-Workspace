package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.platform.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.platform.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.TaskWorkManager;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.IComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ExecutionCallback;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.environment.task.thread.ExecutionRunnable;
import at.ac.tuwien.e0426099.simulator.exceptions.CantStartException;
import at.ac.tuwien.e0426099.simulator.exceptions.RunOnIllegalStateException;
import at.ac.tuwien.e0426099.simulator.util.Log;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class ComputationalSubTask implements IComputationalSubTask,ExecutionCallback {
	private Log log = new Log(this,G.VERBOSE_LOG_MODE_GENERAL && G.VERBOSE_LOG_MODE_SUBTASK);

	private SubTaskId id;
	private PlatformId platformId;
	private String readAbleName;
	private volatile SubTaskStatus status;
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
		type = TaskType.PROCESSING;
		memoryDemand = neededForExecution;
		availableProcPower =new RawProcessingPower(0);
		processingHandicap=0;
		requirements = new ProcessingRequirements(new RawProcessingPower(maxComputationalUtilization),computationsNeededForFinishing);
		taskWorkManager = new TaskWorkManager(requirements);
		listeners = new ArrayList<ITaskListener>();
		startStopSemaphore = new Semaphore(0, true); //initialise with 0, so first aquire waits
		checkOnFinishSemaphore =new Semaphore(1,true);
		log.refreshData();
		log.i("Task created [Type:"+type+", MinExecTime: "+requirements.getMinimumExecutionTimeMs()+"ms, MemDemand: "+memoryDemand+"]");
		setStatus(SubTaskStatus.NOT_STARTED);
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

	public PlatformId getPlatformId() {
		return platformId;
	}

	@Override
	public void setPlatformId(PlatformId id) {
		platformId = id;
		log.refreshData();
	}

	@Override
	public synchronized void pause() {
		if(status == SubTaskStatus.RUNNING) {
			setStatus(SubTaskStatus.PAUSED);
			interruptExecThread();
			aquireSempahore(startStopSemaphore,"startStopPause()");
		} else {
			log.w("Can't pause while not running");
		}
	}

	@Override
	public synchronized void run() {
        if(status == SubTaskStatus.FINISHED) {
            log.w("This seems to be a conccurrent error, nothing to worry about that much: try to run in FINISH state. The run attempt will be ignored.");
        } else {
            aquireSempahore(checkOnFinishSemaphore,"checkFinishRun()");
            if(status == SubTaskStatus.NOT_STARTED || status == SubTaskStatus.PAUSED) {
                setStatus(SubTaskStatus.RUNNING);
                futureRefForThread = G.get().getPlatform(platformId).getThreadPool().submit(new ExecutionRunnable(availableProcPower.getEstimatedTimeInMsToFinish(taskWorkManager.getComputationsLeftToDo()),this));
            } else {
                throw new RunOnIllegalStateException("Can only start running when in pause or not started, but was in state "+status+" in "+ toString());
            }
            aquireSempahore(startStopSemaphore,"startStopRun()");
            releaseSempahore(checkOnFinishSemaphore,"checkFinishRun()");
        }
	}

	@Override
	public synchronized void fail(Exception e) {
		aquireSempahore(checkOnFinishSemaphore,"checkFinsihFail()");
		log.i("Task failed. ["+e.getClass().getSimpleName()+"]");
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
			throw new RunOnIllegalStateException("Trying to start while still running (Status: "+status+") in task "+ toString());
		}
		log.i("Start task. Estimated time to finish: " + timeToSleep+"ms");
		releaseSempahore(startStopSemaphore,"startStopExecRun");
	}

	@Override
	public void onExecFinished() {
		aquireSempahore(checkOnFinishSemaphore,"checkFinishOnExecFinsish");
		taskWorkManager.stopCurrentProcessing();
		if(taskWorkManager.getComputationsLeftToDo() <= 0) {
			log.i("Task Finished. [Net time spent: "+String.valueOf(taskWorkManager.getNetTimeSpendOnComputation())+"ms," +
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
			log.i("Task paused. Time spent since last start: "+((taskWorkManager.getRecentSlice() != null) ? taskWorkManager.getRecentSlice().getActualTimeSpendOnComputation(): "null ")+"ms");
		} else {
			log.w("Task interrupted but not from our Framework, that's strange. Status: "+status);
		}
		releaseSempahore(startStopSemaphore,"startStopExecInterrupt");
	}

	@Override
	public void onExecException(Exception e) {
		taskWorkManager.stopCurrentProcessing();
		setStatus(SubTaskStatus.CONCURRENT_ERROR);
		exception=e;
		log.e("Exception thrown while executing Thread",e);
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
        return "["+platformId+"|"+type+"|"+readAbleName+"/"+id.getSubTaskId().toString().substring(0,5)+"|"+status+"]";
    }

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.TAB+toString());
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
		log.v("interrupt called");
		if(futureRefForThread != null) {
			if(!futureRefForThread.cancel(true)) {
				releaseSempahore(startStopSemaphore,"startStopInterrupt"); //release if it cant be canceled, since it would never get to callbacks
			}
		}
		futureRefForThread =null; //can only interrupt once
	}

	private synchronized void setStatus(SubTaskStatus t) {
		log.d("Status change from "+status+" to " + t);
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
        log.v("Start acquiring semaphore: "+msg);
        s.acquireUninterruptibly();
        log.v("Done acquiring "+msg);
	}

	private void releaseSempahore(Semaphore s, String msg) {
		log.v("Release semaphore: "+msg);
		s.release();
	}
}
