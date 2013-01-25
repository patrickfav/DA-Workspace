package at.ac.tuwien.e0426099.simulator.environment.task.thread;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ExecutionCallback;
import at.ac.tuwien.e0426099.simulator.util.Log;

/**
 * This is the actual "execution" which will be simulated by a sleep
 *
 * @author PatrickF
 * @since 09.12.12
 */
public class ExecutionRunnable implements Runnable{
	private Log log = new Log(this,G.VERBOSE_LOG_MODE_GENERAL && G.VERBOSE_LOG_MODE_SLEEPTHREAD);
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
			log.v("Thread start sleep "+timeToExecute);
			Thread.sleep(Math.max(0,timeToExecute));
			log.v("Thread end sleep "+timeToExecute);
			executionCallback.onExecFinished();
		} catch (InterruptedException e) {
			log.v("Interrupt caught");
			executionCallback.onExecInterrupted();
		} catch (Exception e) {
			log.v("Exception caught");
			executionCallback.onExecException(e);
		}
	}
}
