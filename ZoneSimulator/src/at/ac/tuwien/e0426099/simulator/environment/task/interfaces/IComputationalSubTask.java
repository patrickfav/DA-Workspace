package at.ac.tuwien.e0426099.simulator.environment.task.interfaces;

import at.ac.tuwien.e0426099.simulator.environment.platform.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.RawProcessingPower;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public interface IComputationalSubTask extends ISubTask{
	public void updateAvailableProcessingPower(RawProcessingPower processingPower);
	public void setProcessingHandicap(double percentage);

	public RawProcessingPower getCurrentlyAssignedProcessingPower();

	public ProcessingRequirements getProcessingRequirements();
	public MemoryAmount getMemoryDemand();
}
