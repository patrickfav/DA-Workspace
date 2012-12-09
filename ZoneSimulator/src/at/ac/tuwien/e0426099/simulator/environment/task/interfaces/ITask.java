package at.ac.tuwien.e0426099.simulator.environment.task.interfaces;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public interface ITask {

	public IComputationalSubTask getSubTaskById(UUID id);

	public UUID getId();
	public String getReadAbleName();
}
