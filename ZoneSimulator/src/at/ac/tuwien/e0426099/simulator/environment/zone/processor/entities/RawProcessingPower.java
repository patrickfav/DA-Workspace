package at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities;

/**
 * Wrapper for computations for a given time (ms in this case)
 * @author PatrickF
 * @since 07.12.12
 */
public class RawProcessingPower {
	private long computationsPerMs;

	public RawProcessingPower(long computationsPerMs) {
		this.computationsPerMs = computationsPerMs;
	}

	public long getComputationsPerMs() {
		return computationsPerMs;
	}

	/**
	 * Returns the processing power with given penalty.
	 * Penalty is a double between 0.0 and 1.0 where
	 * 1.0 means 100% meaning 0 processing power and
	 * 0.0 means full power.
	 *
	 * @param penalty
	 * @return
	 */
	public double getComputationsPerMsForPenalty(double penalty) {
		if(penalty >= 1) {
			return 0;
		} else if(penalty <= 0) {
			return getComputationsPerMs();
		} else {
			return Math.floor(((double) computationsPerMs)*penalty);
		}
	}

	public long getComputationsDone(long timeInMs) {
		return computationsPerMs * timeInMs;
	}

	public long getEstimatedTimeInMsToFinish(ProcessingRequirements requirements) {
		long computationsPerMs = Math.min(requirements.getMaxComputationalUtilization().getComputationsPerMs(),this.computationsPerMs);
		return (long) Math.ceil(requirements.getComputationNeedForCompletion() / computationsPerMs);
	}

	public long getEstimatedTimeInMsToFinish(long computationsNeeded) {
		if(computationsPerMs > 0)
			return Math.max(0,(long) Math.ceil(computationsNeeded / computationsPerMs));

		return 0;
	}

    @Override
    public String toString() {
        return String.valueOf(computationsPerMs)+" cycles-per-ms";    //To change body of overridden methods use File | Settings | File Templates.
    }
}
