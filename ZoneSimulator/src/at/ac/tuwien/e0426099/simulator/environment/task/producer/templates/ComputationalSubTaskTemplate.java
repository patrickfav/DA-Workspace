package at.ac.tuwien.e0426099.simulator.environment.task.producer.templates;

import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.math.distributions.IRandomDistribution;

/**
 * @author PatrickF
 * @since 25.01.13
 */
public class ComputationalSubTaskTemplate implements ISubTaskTemplate {
	private String readAbleName;
	private IRandomDistribution neededMemoryInKiB;
	private IRandomDistribution maxComputationalUtilization;
	private IRandomDistribution  computationsNeededForFinishing;

	public ComputationalSubTaskTemplate(String readAbleName, IRandomDistribution neededMemeoryInKiB, IRandomDistribution maxComputationalUtilization, IRandomDistribution computationsNeededForFinishing) {
		this.readAbleName = readAbleName;
		this.neededMemoryInKiB = neededMemeoryInKiB;
		this.maxComputationalUtilization = maxComputationalUtilization;
		this.computationsNeededForFinishing = computationsNeededForFinishing;
	}

	public String getReadAbleName() {
		return readAbleName;
	}

	public IRandomDistribution getNeededMemoryInKiB() {
		return neededMemoryInKiB;
	}

	public IRandomDistribution getMaxComputationalUtilization() {
		return maxComputationalUtilization;
	}

	public IRandomDistribution getComputationsNeededForFinishing() {
		return computationsNeededForFinishing;
	}

	@Override
	public ISubTask.TaskType getTaskType() {
		return ISubTask.TaskType.PROCESSING;
	}
}
