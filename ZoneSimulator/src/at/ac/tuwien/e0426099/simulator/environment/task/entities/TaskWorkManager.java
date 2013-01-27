package at.ac.tuwien.e0426099.simulator.environment.task.entities;

import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.exceptions.CantStartException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Responsible for keeping track of the work that has been done and the time needed
 *
 * @author PatrickF
 * @since 09.12.12
 */
public class TaskWorkManager {
	private ProcessingRequirements processingRequirements;
	private List<ProcessingSlice> processingSlices;
	private long computationsLeftToDo;

	public TaskWorkManager(ProcessingRequirements processingRequirements) {
		this.processingRequirements = processingRequirements;
		processingSlices = Collections.synchronizedList(new ArrayList<ProcessingSlice>());
		computationsLeftToDo = processingRequirements.getComputationNeedForCompletion();
	}

	/**
	 * Start a new intervall/sloce
	 * @param startTime time started
	 * @param givenProcessingPower how mach cycles per ms
	 * @return
	 * @throws CantStartException
	 */
	public synchronized long startProcessing(Date startTime, RawProcessingPower givenProcessingPower,double executionFactor) throws CantStartException {
		if(processingSlices.size() > 0) {
			if(getRecentSlice().isStillProcessing()) {
				throw new CantStartException("Cannot start processing while already in processing");
			}
			if(givenProcessingPower.getComputationsPerMs() > processingRequirements.getMaxComputationalUtilization().getComputationsPerMs()) {
				throw new CantStartException("Cannot process with that much power. Only so much possible as ProcessingRequirements state");
			}
		}

		ProcessingSlice slice = new ProcessingSlice(startTime,givenProcessingPower,computationsLeftToDo,executionFactor);
		processingSlices.add(slice);

		return slice.getEstimatedTimeInMsForFinish();
	}

	/**
	 * Stops current interval/slice
	 * Will throw RuntimeException if not currently running
	 */
	public synchronized void stopCurrentProcessing() {
		if(processingSlices.size() < 0) {
			throw new RuntimeException("No slice available to stop");
		}

		computationsLeftToDo -= processingSlices.get(processingSlices.size()-1).endProcessing();
	}

	public synchronized long getComputationsLeftToDo() {
		return computationsLeftToDo;
	}

	/**
	 * The net time for computations (does not add the pauses between intervals/slices)
	 * @return
	 */
	public synchronized long getNetTimeSpendOnComputation() {
		long timeMs = 0;
		for(ProcessingSlice s:processingSlices) {
			timeMs += s.getActualTimeSpendOnComputation();
		}
		return timeMs;
	}

	/**
	 * Overall duration from slice1.start to sliceN.end
	 * @return
	 */
	public synchronized long getOverallTimeSpendOnComputation() {
		if(processingSlices.size() <= 0) {
			return 0;
		} else if(processingSlices.size() == 1) {
			return processingSlices.get(0).getActualTimeSpendOnComputation();
		} else {
			return getRecentSlice().getEndTime().getTime() - processingSlices.get(0).getStartTime().getTime();
		}
	}

	/**
	 * Returns the sum of done computations accoring to the slices
	 * @return
	 */
	public synchronized long getOverallComputationsDone() {
		long sum = 0;
		for(ProcessingSlice p:processingSlices) {
			sum += (double) p.getActualComputationsDone();
		}
		return sum;
	}

	public synchronized ProcessingSlice getRecentSlice() {
		if(processingSlices.size() > 0) {
			return processingSlices.get(processingSlices.size()-1);
		}

		return null;
	}

    public List<ProcessingSlice> getProcessingSlices() {
        return processingSlices;
    }


	/**
	 * Returns the avg. execution time factor combined for all slices
	 * Uses a weighted algorithm.
	 *
	 * @return
	 */
	public double getWeightedExecutionFactorAvg() {
		double avg = 0;
		for(ProcessingSlice p:processingSlices) {
			avg += (double) p.getActualComputationsDone() * p.getExecutionFactor();
		}

		return avg / (double) getOverallComputationsDone();
	}
    /* ***************************************************************************** PRIVATES */
}
