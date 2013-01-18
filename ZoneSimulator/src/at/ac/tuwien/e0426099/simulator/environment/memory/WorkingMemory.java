package at.ac.tuwien.e0426099.simulator.environment.memory;

import at.ac.tuwien.e0426099.simulator.environment.GodClass;
import at.ac.tuwien.e0426099.simulator.environment.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.memory.entities.TaskInMemory;
import at.ac.tuwien.e0426099.simulator.environment.processor.listener.TaskManagementMemoryListener;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Basically the RAM of the System
 * @author PatrickF
 * @since 07.12.12
 */
public class WorkingMemory implements TaskManagementMemoryListener {
	private static final boolean SHUFFLING_TASKS_BEFORE_ASSIGNMENT = true;

	private PlatformId platformId;
	private double memoryNotAssignedPenalityMultiplicator; //to configure penality for too low ram
	private MemoryAmount sizeOfMemory;
	private List<TaskInMemory> taskInMemoryList;
	private List<ChangedMemoryListener> memoryListeners;

	public WorkingMemory(MemoryAmount sizeOfMemory,double memoryNotAssignedPenalityMultiplicator) {
		this.memoryNotAssignedPenalityMultiplicator = memoryNotAssignedPenalityMultiplicator;
		this.sizeOfMemory = sizeOfMemory;
		taskInMemoryList = new ArrayList<TaskInMemory>();
		memoryListeners = new ArrayList<ChangedMemoryListener>();
	}

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

	public void setPlatformId(PlatformId platformId) {
		this.platformId = platformId;
	}

	/* ********************************************************************************** PRIVATE */

	private void addSubTask(SubTaskId subTaskId) {
		TaskInMemory tim;
		if(getFreeMemory().getAmountInKiloByte() > 0) { //has some free mem to give
			if(getFreeMemory().getAmountInKiloByte() >= GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(subTaskId).getMemoryDemand().getAmountInKiloByte()) { //more memory than needed
				tim = new TaskInMemory(platformId,subTaskId, GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(subTaskId).getMemoryDemand(),new MemoryAmount(0));
			} else { //can only assign the rest of free mem
				tim = new TaskInMemory(platformId,subTaskId,getFreeMemory(),
						new MemoryAmount(GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(subTaskId).getMemoryDemand().getAmountInKiloByte()-getFreeMemory().getAmountInKiloByte()));
			}
		} else { //no memory to give
			tim = new TaskInMemory(platformId,subTaskId,new MemoryAmount(0), GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(subTaskId).getMemoryDemand());
		}
		tim.markChanged();
		taskInMemoryList.add(tim);
		GodClass.instance().getPlatform(platformId).getSubTaskForProcessor(subTaskId).setProcessingHandicap(tim.getRatioNotAssigned()*memoryNotAssignedPenalityMultiplicator);
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
