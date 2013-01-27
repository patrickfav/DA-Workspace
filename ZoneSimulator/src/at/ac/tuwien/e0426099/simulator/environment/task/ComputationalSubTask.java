package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.Env;
import at.ac.tuwien.e0426099.simulator.environment.EnvConst;
import at.ac.tuwien.e0426099.simulator.environment.task.producer.templates.ComputationalSubTaskTemplate;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.environment.zone.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.TaskWorkManager;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.IComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ExecutionCallback;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;
import at.ac.tuwien.e0426099.simulator.environment.task.thread.ExecutionRunnable;
import at.ac.tuwien.e0426099.simulator.exceptions.CantStartException;
import at.ac.tuwien.e0426099.simulator.exceptions.RunOnIllegalStateException;
import at.ac.tuwien.e0426099.simulator.helper.Log;
import at.ac.tuwien.e0426099.simulator.helper.util.LogUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class ComputationalSubTask implements IComputationalSubTask,ExecutionCallback {
	private Log log = new Log(this, EnvConst.VERBOSE_LOG_MODE_GENERAL && EnvConst.VERBOSE_LOG_MODE_SUBTASK);

	private SubTaskId id;
	private ZoneId zoneId;
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
	private double executionFactor;

	private Semaphore startStopSemaphore;
	private Semaphore pauseSemaphore;
	private Semaphore failSemaphore;
	private Semaphore checkOnFinishSemaphore;
	private Semaphore duringAnyKindOfWork;


	/**
	 * @param readAbleName
	 * @param neededForExecution memory needed for execution
	 * @param maxComputationalUtilization whats the max value of compuationsPerMs
	 * @param computationsNeededForFinishing how many cycles/computation need to finsih
	 */
	public ComputationalSubTask(String readAbleName, MemoryAmount neededForExecution, Long maxComputationalUtilization, Long computationsNeededForFinishing) {
		id = new SubTaskId();
		this.readAbleName = readAbleName;
		type = TaskType.PROCESSING;
		memoryDemand = neededForExecution;
		availableProcPower =new RawProcessingPower(0);
		processingHandicap=0;
		executionFactor=1.0;
		requirements = new ProcessingRequirements(new RawProcessingPower(maxComputationalUtilization),computationsNeededForFinishing);
		taskWorkManager = new TaskWorkManager(requirements);
		listeners = new ArrayList<ITaskListener>();
		log.refreshData();
		log.i("Task created [Type:"+type+", MinExecTime: "+requirements.getMinimumExecutionTimeMs()+"ms, MemDemand: "+memoryDemand+"]");
		setStatus(SubTaskStatus.NOT_STARTED);

		startStopSemaphore = new Semaphore(1, true);
		pauseSemaphore = new Semaphore(1, true);
		checkOnFinishSemaphore =new Semaphore(1,true);
		failSemaphore=new Semaphore(1,true);
		duringAnyKindOfWork=new Semaphore(1,true);
	}

	/**
	 * Create from temlpate
	 * @param template
	 */
	public ComputationalSubTask(ComputationalSubTaskTemplate template) {
		this(template.getReadAbleName(),
                new MemoryAmount(template.getNeededMemoryInKiB().getNext().longValue()),
                template.getMaxComputationalUtilization().getNext().longValue(),
                template.getComputationsNeededForFinishing().getNext().longValue());
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

	public ZoneId getZoneId() {
		return zoneId;
	}

	@Override
	public void setZoneId(ZoneId id) {
		zoneId = id;
		log.refreshData();
	}

	@Override
	public void setExecutionFactor(double factor) {
		log.d("Set executionfactor to "+factor);
		executionFactor = factor;
	}

	@Override
	public void pause() {
		aquireSempahore(duringAnyKindOfWork,"anyPause()");
		aquireSempahore(failSemaphore,"failPause()");
		aquireSempahore(startStopSemaphore,"pausePause()");
		aquireSempahore(checkOnFinishSemaphore,"checkFinishPause()");
		if(status == SubTaskStatus.RUNNING) {
			setStatus(SubTaskStatus.PAUSED);
			interruptExecThread();
			aquireSempahore(pauseSemaphore,"startStopPause()");
		} else {
			log.w("Can't pause while not running");
		}
		releaseSempahore(checkOnFinishSemaphore,"checkFinishPause()");
		releaseSempahore(startStopSemaphore,"pausePause()");
		releaseSempahore(failSemaphore, "failPause()");
		releaseSempahore(duringAnyKindOfWork, "anyPause()");
	}

	@Override
	public void run() {
		aquireSempahore(duringAnyKindOfWork,"anyRun()");
		aquireSempahore(failSemaphore,"failRun()");
		aquireSempahore(pauseSemaphore,"pauseRun()");
		aquireSempahore(checkOnFinishSemaphore,"checkFinishRun()");
        if(status == SubTaskStatus.FINISHED) {
            log.w("This seems to be a conccurrent error, nothing to worry about that much: try to run in FINISH state. The run attempt will be ignored.");
        } else {

            if(status == SubTaskStatus.NOT_STARTED || status == SubTaskStatus.PAUSED) {
                setStatus(SubTaskStatus.RUNNING);
				futureRefForThread = Env.get().getZone(zoneId).getThreadPool().submit(new ExecutionRunnable(availableProcPower.getEstimatedTimeInMsToFinish((long)(taskWorkManager.getComputationsLeftToDo()/executionFactor)),this));
            } else {
                throw new RunOnIllegalStateException("Can only start running when in pause or not started, but was in state "+status+" in "+ toString());
            }
			aquireSempahore(startStopSemaphore,"startStopRun()");
        }
		releaseSempahore(checkOnFinishSemaphore,"checkFinishRun()");
		releaseSempahore(pauseSemaphore, "pauseRun()");
		releaseSempahore(failSemaphore,"failRun()");
		releaseSempahore(duringAnyKindOfWork, "anyFail()");
	}

	@Override
	public void fail(Exception e) {
		aquireSempahore(duringAnyKindOfWork,"anyFail()");
		aquireSempahore(startStopSemaphore,"startStopFail()");
		aquireSempahore(pauseSemaphore, "pauseFail()");
		aquireSempahore(checkOnFinishSemaphore, "checkFinsihFail()");
		log.i("Task failed. [" + e.getClass().getSimpleName() + "]");
		setStatus(SubTaskStatus.SIMULATED_ERROR);
		exception=e;
		interruptExecThread();
		aquireSempahore(failSemaphore, "failFail()");
		releaseSempahore(checkOnFinishSemaphore, "checkFinsihFail()");
		releaseSempahore(pauseSemaphore, "pauseFail()");
		releaseSempahore(startStopSemaphore, "startStopFail()");
		releaseSempahore(duringAnyKindOfWork, "anyFail()");
	}

	@Override
	public void updateAvailableProcessingPower(RawProcessingPower resources) {
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
			timeToSleep = taskWorkManager.startProcessing(new Date(),availableProcPower,executionFactor);
		} catch (CantStartException e) {
			throw new RunOnIllegalStateException("Trying to start while still running (Status: "+status+") in task "+ toString());
		}
		log.i("Start task. Estimated time to finish: " + timeToSleep+"ms");
		releaseSempahore(startStopSemaphore, "startStopExecRun");
	}

	@Override
	public void onExecFinished() {
		aquireSempahore(checkOnFinishSemaphore, "checkFinishOnExecFinsish");
		taskWorkManager.stopCurrentProcessing();
		if(taskWorkManager.getComputationsLeftToDo() <= 0) {
			log.i("Task Finished. [Net time spent: "+String.valueOf(taskWorkManager.getNetTimeSpendOnComputation())+"ms," +
					" Overall spent: "+String.valueOf(taskWorkManager.getOverallTimeSpendOnComputation())+"ms]");
			setStatus(SubTaskStatus.FINISHED);
		}

		releaseSempahore(checkOnFinishSemaphore, "checkFinishOnExecFinsish");

		if(taskWorkManager.getComputationsLeftToDo() <= 0) {
			callAllListenerFinished();
		}
	}

	@Override
	public void onExecInterrupted() {
		if(status == SubTaskStatus.PAUSED) {
			taskWorkManager.stopCurrentProcessing();
			log.i("Task paused. Time spent since last start: "+((taskWorkManager.getRecentSlice() != null) ? taskWorkManager.getRecentSlice().getActualTimeSpendOnComputation(): "null ")+"ms");
			releaseSempahore(pauseSemaphore, "startStopExecInterrupt");
		} else if(status == SubTaskStatus.SIMULATED_ERROR) {
			taskWorkManager.stopCurrentProcessing();
			releaseSempahore(failSemaphore, "failException");
			callAllListenerFailed();
		} else {
			log.w("Task interrupted but not from our Framework, that's strange. Status: "+status);
		}
	}

	@Override
	public void onExecException(Exception e) {
		taskWorkManager.stopCurrentProcessing();
		setStatus(SubTaskStatus.CONCURRENT_ERROR);
		exception=e;
		log.e("Exception thrown while executing Thread",e);
		releaseSempahore(failSemaphore, "failException");
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
        return "["+ zoneId +"|"+type+"|"+readAbleName+"/"+id.getSubTaskId().toString().substring(0,5)+"|"+status+"]";
    }

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.TAB+toString());
        sb.append(LogUtil.TAB+" Status: "+status+", Needed Memory: "+memoryDemand+", ProcReqs: "+requirements+ LogUtil.BR);
        sb.append(LogUtil.TAB+LogUtil.TAB+"Summary: Net time spent: "+
				String.valueOf(taskWorkManager.getNetTimeSpendOnComputation())+"ms, Overall: "+
				String.valueOf(taskWorkManager.getOverallTimeSpendOnComputation())+"ms, Min: "+
				requirements.getMinimumExecutionTimeMs()+" ms, Avg Factor: x"+(Math.round(taskWorkManager.getWeightedExecutionFactorAvg()*100.0)/100.0)+ LogUtil.BR);


        if(detailed)
            for(int i=0; i< taskWorkManager.getProcessingSlices().size();i++) {
                sb.append(LogUtil.TAB+LogUtil.TAB+LogUtil.TAB+" Processingslice "+(i+1)+": "+taskWorkManager.getProcessingSlices().get(i)+ LogUtil.BR);
            }

        return sb.toString();
    }

	/* ***************************************************************************** PRIVATES */

	/**
	 * This will interrupt the running thread simulating a pause or fail
	 */
	private void interruptExecThread() {
		log.v("interrupt called");
		if(futureRefForThread != null) {
			if(!futureRefForThread.cancel(true)) {
				releaseSempahore(startStopSemaphore, "startStopInterrupt"); //release if it cant be canceled, since it would never get to callbacks
			}
		}
		futureRefForThread =null; //can only interrupt once
	}

	private void setStatus(SubTaskStatus t) {
		log.v("Status change from " + status + " to " + t);
		status = t;
		log.refreshData();
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
        log.v("Start acquiring semaphore: " + msg);
		try {
			s.tryAcquire(EnvConst.SUBTASK_WAIT_TIMEOUT_SEC, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.v("Interrupt while waiting for semaphore "+msg);
		}
		log.v("Done acquiring "+msg);
	}

	private void releaseSempahore(Semaphore s, String msg) {
		log.v("Release semaphore: "+msg);
		s.release();
	}
}
