package at.ac.tuwien.e0426099.simulator.environment.platform.memory.entities;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.platform.ZoneId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class TaskInMemory {
	private SubTaskId subTaskId;
	private ZoneId zoneId;
	private MemoryAmount amountAssignedToMemory;
	private MemoryAmount amountNotAssignable;
	private boolean somethingHasChanged;

	public TaskInMemory(ZoneId zoneId,SubTaskId subTaskId, MemoryAmount amountAssignedToMemory, MemoryAmount amountNotAssignable) {
		this.subTaskId = subTaskId;
		this.amountAssignedToMemory = amountAssignedToMemory;
		this.amountNotAssignable = amountNotAssignable;
		somethingHasChanged=false;
		this.zoneId = zoneId;
	}

	public SubTaskId getSubTaskId() {
		return subTaskId;
	}

	public void setSubTaskId(SubTaskId subTaskId) {
		this.subTaskId = subTaskId;
	}

	public MemoryAmount getAmountAssignedToMemory() {
		return amountAssignedToMemory;
	}

	public void setAmountAssignedToMemory(MemoryAmount amountAssignedToMemory) {
		this.amountAssignedToMemory = amountAssignedToMemory;
	}

	public MemoryAmount getAmountNotAssignable() {
		return amountNotAssignable;
	}

	public void setAmountNotAssignable(MemoryAmount amountNotAssignable) {
		this.amountNotAssignable = amountNotAssignable;
	}

	public void markChanged() {
		somethingHasChanged=true;
	}
	public void unMarkChanged() {
		somethingHasChanged=false;
	}
	public boolean isSomethingHasChanged() {
		return somethingHasChanged;
	}

	/**
	 * How much memory needed by task execution did not fit in memory (RAM)
	 * @return [0.0,1.0]
	 */
	public double getRatioNotAssigned() {
		return (double) amountNotAssignable.getAmountInKiloByte() / (double) G.get().getPlatform(zoneId).getSubTaskForProcessor(subTaskId).getMemoryDemand().getAmountInKiloByte();
	}
}
