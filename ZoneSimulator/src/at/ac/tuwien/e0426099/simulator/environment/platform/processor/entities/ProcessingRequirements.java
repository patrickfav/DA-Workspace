package at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingRequirements {
	private RawProcessingPower maxComputationalUtilization;
	private Long computationNeedForCompletion;

	public ProcessingRequirements(RawProcessingPower maxComputationalUtilization, Long msNeededToFinishWithMaxUtilization) {
		this.maxComputationalUtilization = maxComputationalUtilization;
		this.computationNeedForCompletion = msNeededToFinishWithMaxUtilization;
	}

	public RawProcessingPower getMaxComputationalUtilization() {
		return maxComputationalUtilization;
	}

	public void setMaxComputationalUtilization(RawProcessingPower maxComputationalUtilization) {
		this.maxComputationalUtilization = maxComputationalUtilization;
	}

	public Long getComputationNeedForCompletion() {
		return computationNeedForCompletion;
	}

	public void setComputationNeedForCompletion(Long computationNeedForCompletion) {
		this.computationNeedForCompletion = computationNeedForCompletion;
	}

	public long getMinimumExecutionTimeMs() {
		return maxComputationalUtilization.getEstimatedTimeInMsToFinish(this);
	}

    @Override
    public String toString() {
        return "MaxUtil: "+maxComputationalUtilization+", NeededCPUCycles: "+computationNeedForCompletion;
    }
}
