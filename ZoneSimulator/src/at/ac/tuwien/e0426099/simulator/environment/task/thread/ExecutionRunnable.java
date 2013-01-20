package at.ac.tuwien.e0426099.simulator.environment.task.thread;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ExecutionCallback;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author PatrickF
 * @since 09.12.12
 */
public class ExecutionRunnable implements Runnable{
	private Logger log = LogManager.getLogger(ExecutionRunnable.class.getName());
	private long timeToExecute;
	private ExecutionCallback executionCallback;

	public ExecutionRunnable() {}

	public ExecutionRunnable(long timeToExecute, ExecutionCallback executionCallback) {
		this.timeToExecute=timeToExecute;
		this.executionCallback = executionCallback;
	}

	@Override
	public void run() {
		try {
			executionCallback.onExecRun();
			if(G.VERBOSE_LOG_MODE)
				log.debug("Thread start sleep "+timeToExecute);
			Thread.sleep(Math.max(0,timeToExecute));
			executionCallback.onExecFinished();
		} catch (InterruptedException e) {
			executionCallback.onExecInterrupted();
		} catch (Exception e) {
			executionCallback.onExecException(e);
		}
	}

	public long getTimeToExecute() {
		return timeToExecute;
	}

	public void setTimeToExecute(long timeToExecute) {
		this.timeToExecute = timeToExecute;
	}

	public ExecutionCallback getExecutionCallback() {
		return executionCallback;
	}

	public void setExecutionCallback(ExecutionCallback executionCallback) {
		this.executionCallback = executionCallback;
	}


}
