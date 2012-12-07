package at.ac.tuwien.e0426099.simulator.environment.memory.entities;

import at.ac.tuwien.e0426099.simulator.environment.task.IRunnableTask;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class TaskInMemory {
	private IRunnableTask task;
	private MemoryAmount amountAssignedToMemory;
	private MemoryAmount amountNotAssignable;

	public TaskInMemory(IRunnableTask task, MemoryAmount amountAssignedToMemory, MemoryAmount amountNotAssignable) {
		this.task = task;
		this.amountAssignedToMemory = amountAssignedToMemory;
		this.amountNotAssignable = amountNotAssignable;
	}

	public IRunnableTask getTask() {
		return task;
	}

	public void setTask(IRunnableTask task) {
		this.task = task;
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


	/**
	 * How much memory needed by task execution did not fit in memory (RAM)
	 * @return [0.0,1.0]
	 */
	public double getRatioNotAssigned() {
		return (double) amountNotAssignable.getAmountInKiloByte() / (double) task.getMemoryDemand().getAmountInKiloByte();
	}
}
