package at.ac.tuwien.e0426099.simulator.environment.processor.entities;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingRequirements {
	private Long maxComputationalUtilization;
	private Long msNeededToFinishWithMaxUtilization;

	public Long getMaxComputationalUtilization() {
		return maxComputationalUtilization;
	}

	public void setMaxComputationalUtilization(Long maxComputationalUtilization) {
		this.maxComputationalUtilization = maxComputationalUtilization;
	}

	public Long getMsNeededToFinishWithMaxUtilization() {
		return msNeededToFinishWithMaxUtilization;
	}

	public void setMsNeededToFinishWithMaxUtilization(Long msNeededToFinishWithMaxUtilization) {
		this.msNeededToFinishWithMaxUtilization = msNeededToFinishWithMaxUtilization;
	}
}
