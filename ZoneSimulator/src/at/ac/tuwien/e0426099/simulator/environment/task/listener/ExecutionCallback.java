package at.ac.tuwien.e0426099.simulator.environment.task.listener;

/**
 * @author PatrickF
 * @since 09.12.12
 */
public interface ExecutionCallback {
	public void onExecRun();
	public void onExecFinished();
	public void onExecInterrupted();
	public void onExecException(Exception e);
}
