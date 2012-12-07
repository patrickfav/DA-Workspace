package at.ac.tuwien.e0426099.simulator.simulation.task;

import at.ac.tuwien.e0426099.simulator.simulation.processor.ProcessingRequirements;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public interface IRunnableTask {
	public ProcessingRequirements getProcessingRequirements();
	public void pauseProcessing();
	public void updateProcessingResources(long processingPower);
	public void run();
	public void setTaskListener(ITaskListener listener);
	public UUID getId();
	public String getReadAbleName();
}
