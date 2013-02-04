package at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities;

/**
 * Wrapper for computations for a given time (ms in this case)
 * @author PatrickF
 * @since 07.12.12
 */
public class RawProcessingPower {
	private long computationsPerMs;
	private double penalty;

	public RawProcessingPower(long computationsPerMs) {
		this.computationsPerMs = computationsPerMs;
		penalty=0;
	}

	public RawProcessingPower(long computationsPerMs, long penality) {
		this.computationsPerMs = computationsPerMs;
		this.penalty = penality;
	}

	public long getComputationsPerMs() {
		return computationsPerMs;
	}

	public double getPenalty() {
		return penalty;
	}

	public void setPenalty(double penalty) {
		this.penalty = penalty;
	}

	/**
	 * Returns the processing power with given penalty.
	 * Penalty is a double between 0.0 and 1.0 where
	 * 1.0 means 100% meaning 0 processing power and
	 * 0.0 means full power. The count of concurrent
	 * Threads will be subtracted by 1 (since one has
	 * no penalty)
	 *
	 * @return
	 */
	public double getComputationsPerMsForPenalty(int concurrentThreads) {
		if(penalty >= 1) {
			return 0;
		} else if(penalty <= 0) {
			return getComputationsPerMs();
		} else {
			return computationsPerMs - Math.max(0,Math.round(((double) computationsPerMs)*(penalty*Math.max(0,(double)concurrentThreads - 1.0))));
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
