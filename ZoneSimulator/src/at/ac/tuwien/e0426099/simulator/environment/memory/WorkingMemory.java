package at.ac.tuwien.e0426099.simulator.environment.memory;

import at.ac.tuwien.e0426099.simulator.environment.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.memory.entities.TaskInMemory;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.TaskManagementListener;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

import java.util.List;

/**
 * Basically the RAM of the System
 * @author PatrickF
 * @since 07.12.12
 */
public class WorkingMemory implements TaskManagementListener {
	private MemoryAmount sizeOfMemory;
	private List<TaskInMemory> taskInMemoryList;
	private List<ChangedMemoryListener> memoryListeners;

	@Override
	public void onSubTaskAdded(SubTaskId subTaskId) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void onSubTaskFinished(SubTaskId subTaskId) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	/* ********************************************************************************** INNER CLASSES */

	public interface ChangedMemoryListener {
		public void subTaskHaveAlteredMemoryAssignement(List<SubTaskId> changedSubTaskIds);
	}
}
