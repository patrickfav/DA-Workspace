package at.ac.tuwien.e0426099.simulator.environment.task.entities;

import at.ac.tuwien.e0426099.simulator.environment.processor.entities.ProcessingRequirements;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.RawProcessingPower;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author PatrickF
 * @since 09.12.12
 */
public class TaskWorkManager {
	private ProcessingRequirements processingRequirements;
	private RawProcessingPower currentProcessingPower;
	private List<ProcessingSlice> processingSlices;
	private long computationsLeftToDo;

	public TaskWorkManager(ProcessingRequirements processingRequirements) {
		this.processingRequirements = processingRequirements;
		processingSlices = new ArrayList<ProcessingSlice>();
		computationsLeftToDo = processingRequirements.getComputationNeedForCompletion();
	}

	public synchronized long startProcessing(Date startTime, RawProcessingPower givenProcessingPower) {
		if(processingSlices.size() > 0) {
			if(getRecentSlice().isStillProcessing()) {
				throw new RuntimeException("Cannot start processing while already in processing");
			}
			if(givenProcessingPower.getComputationsPerMs() > processingRequirements.getMaxComputationalUtilization().getComputationsPerMs()) {
				throw new RuntimeException("Cannot process with that much power. Only so much possible as ProcessingRequirements state");
			}
		}

		ProcessingSlice slice = new ProcessingSlice(startTime,givenProcessingPower,computationsLeftToDo);
		processingSlices.add(slice);

		return slice.getEstimatedTimeInMsForFinish();
	}

	public synchronized void stopCurrentProcessing() {
		if(processingSlices.size() < 0) {
			throw new RuntimeException("No slice available to stop");
		}

		computationsLeftToDo -= processingSlices.get(processingSlices.size()-1).endProcessing();
	}

	public synchronized long getComputationsLeftToDo() {
		return computationsLeftToDo;
	}

	public synchronized long getNetTimeSpendOnComputation() {
		long timeMs = 0;
		for(ProcessingSlice s:processingSlices) {
			timeMs += s.getActualTimeSpendOnComputation();
		}
		return timeMs;
	}

	public synchronized long getOverallTimeSpendOnComputation() {
		if(processingSlices.size() <= 0) {
			return 0;
		} else if(processingSlices.size() == 1) {
			return processingSlices.get(0).getActualTimeSpendOnComputation();
		} else {
			return getRecentSlice().getEndTime().getTime() - processingSlices.get(0).getStartTime().getTime();
		}
	}

	public synchronized ProcessingSlice getRecentSlice() {
		if(processingSlices.size() > 0) {
			return processingSlices.get(processingSlices.size()-1);
		}

		return null;
	}

	/* ***************************************************************************** PRIVATES */
}
