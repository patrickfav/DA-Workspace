package at.ac.tuwien.e0426099.simulator.environment.task;

import at.ac.tuwien.e0426099.simulator.environment.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.task.listener.ITaskListener;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 08.12.12
 */
public class SubTask extends Thread implements ISubTask {
	private SubTaskId id;
	private String readAbleName;
	private TaskStatus status;
	private TaskType type;
	private MemoryAmount memoryDemand;
	private ITaskListener listener;
	private long availableResources;
	private double processingHandicap;
	private ProcessingRequirements requirements;



	public SubTask(UUID parentTaskId, String readAbleName, MemoryAmount neededForExecution,Long maxComputationalUtilization, Long msNeededToFinishWithMaxUtilization) {
		id = new SubTaskId(parentTaskId,UUID.randomUUID());
		this.readAbleName = readAbleName;
		status = TaskStatus.NOT_STARTED;
		type = TaskType.PROCESSING;
		memoryDemand = neededForExecution;
		availableResources=0;
		processingHandicap=0;
		requirements = new ProcessingRequirements(maxComputationalUtilization,msNeededToFinishWithMaxUtilization);
	}

	@Override
	public SubTaskId getSubTaskId() {
		return id;
	}

	@Override
	public String getReadAbleName() {
		return readAbleName;
	}

	@Override
	public void pause() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void run() {
		sleep();
	}

	@Override
	public void fail(Exception e) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void updateResources(long resources) {
		availableResources = resources;
	}

	@Override
	public void setProcessingHandicap(double percentage) {
		processingHandicap = percentage;
	}

	@Override
	public long getCurrentlyAssignedProcessingPower() {
		return availableResources;
	}

	@Override
	public TaskStatus getStatus() {
		return status;
	}

	@Override
	public TaskType getTaskType() {
		return type;
	}

	@Override
	public ProcessingRequirements getProcessingRequirements() {
		return requirements;
	}

	@Override
	public MemoryAmount getMemoryDemand() {
		return memoryDemand;
	}

	@Override
	public void setTaskListener(ITaskListener listener) {
		this.listener = listener;
	}
}
