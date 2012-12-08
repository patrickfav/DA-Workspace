package at.ac.tuwien.e0426099.simulator.environment.task;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public interface ITask {

	public ISubTask getSubTaskById(UUID id);

	public UUID getId();
	public String getReadAbleName();
}
