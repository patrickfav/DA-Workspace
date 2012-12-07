package at.ac.tuwien.e0426099.simulator.environment.task.listener;

import at.ac.tuwien.e0426099.simulator.environment.task.IRunnableTask;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public interface ITaskListener {
	public void onTaskFinished(IRunnableTask task);
}