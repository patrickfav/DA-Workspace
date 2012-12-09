package at.ac.tuwien.e0426099.simulator.environment.memory;

import at.ac.tuwien.e0426099.simulator.environment.Platform;
import at.ac.tuwien.e0426099.simulator.environment.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.memory.entities.TaskInMemory;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.TaskManagementListener;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Basically the RAM of the System
 * @author PatrickF
 * @since 07.12.12
 */
public class WorkingMemory implements TaskManagementListener {
	private static final boolean SHUFFLING_TASKS_BEFORE_ASSIGNMENT = true;

	private double memoryNotAssignedPenalityMultiplicator; //to configure penality for too low ram
	private MemoryAmount sizeOfMemory;
	private List<TaskInMemory> taskInMemoryList;
	private List<ChangedMemoryListener> memoryListeners;

	@Override
	public synchronized void onSubTaskAdded(SubTaskId subTaskId) {
		addSubTask(subTaskId);
	}

	@Override
	public synchronized void onSubTaskFinished(SubTaskId subTaskId) {
		//noinspection SuspiciousMethodCalls
		if(taskInMemoryList.remove(subTaskId)) { //if there was actually smth removed
			List<TaskInMemory> tempList = new ArrayList<TaskInMemory>(taskInMemoryList);
			taskInMemoryList.clear();

			if(SHUFFLING_TASKS_BEFORE_ASSIGNMENT)
				Collections.shuffle(tempList);

			for(TaskInMemory t:tempList) {
				addSubTask(t.getSubTaskId());
			}
		} else { //did not contain the element
		  	//TODO: display log, but still do nothing
		}
	}

	public synchronized void addChangedMemoryListener(ChangedMemoryListener l) {
		memoryListeners.add(l);
	}

	/* ********************************************************************************** PRIVATE */

	private void addSubTask(SubTaskId subTaskId) {
		TaskInMemory tim;
		if(getFreeMemory().getAmountInKiloByte() > 0) { //has some free mem to give
			if(getFreeMemory().getAmountInKiloByte() >= Platform.getInstance().getSubTask(subTaskId).getMemoryDemand().getAmountInKiloByte()) { //more memory than needed
				tim = new TaskInMemory(subTaskId, Platform.getInstance().getSubTask(subTaskId).getMemoryDemand(),new MemoryAmount(0));
			} else { //can only assign the rest of free mem
				tim = new TaskInMemory(subTaskId,getFreeMemory(),
						new MemoryAmount(Platform.getInstance().getSubTask(subTaskId).getMemoryDemand().getAmountInKiloByte()-getFreeMemory().getAmountInKiloByte()));
			}
		} else { //no memory to give
			tim = new TaskInMemory(subTaskId,new MemoryAmount(0), Platform.getInstance().getSubTask(subTaskId).getMemoryDemand());
		}
		tim.markChanged();
		taskInMemoryList.add(tim);
		Platform.getInstance().getSubTask(subTaskId).setProcessingHandicap(tim.getRatioNotAssigned()*memoryNotAssignedPenalityMultiplicator);
	}

	private synchronized MemoryAmount getUsedMemory() {
		long usedMemKiB = 0;
		for(TaskInMemory t:taskInMemoryList) {
			usedMemKiB+= t.getAmountAssignedToMemory().getAmountInKiloByte();
		}
		return new MemoryAmount(usedMemKiB);
	}

	private MemoryAmount getFreeMemory() {
		return new MemoryAmount(sizeOfMemory.getAmountInKiloByte()-getUsedMemory().getAmountInKiloByte());
	}

	private synchronized void callListener() {
		List<SubTaskId> subTaskIdList = new ArrayList<SubTaskId>();
		for(TaskInMemory t:taskInMemoryList) {
			if(t.isSomethingHasChanged()) {
				subTaskIdList.add(t.getSubTaskId());
				t.unMarkChanged();
			}
		}
		for(ChangedMemoryListener l:memoryListeners) {
			l.subTaskHaveAlteredMemoryAssignement(subTaskIdList);
		}
	}

	/* ********************************************************************************** INNER CLASSES */

	public interface ChangedMemoryListener {
		public void subTaskHaveAlteredMemoryAssignement(List<SubTaskId> changedSubTaskIds);
	}
}
