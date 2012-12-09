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

	public long startProcessing(Date startTime, RawProcessingPower givenProcessingPower) {
		if(processingSlices.size() > 0) {
			if(processingSlices.get(processingSlices.size()-1).isStillProcessing()) {
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

	public void stopCurrentProcessing() {
		if(processingSlices.size() < 0) {
			throw new RuntimeException("No slice available to stop");
		}

		computationsLeftToDo -= processingSlices.get(processingSlices.size()-1).endProcessing();
	}
}
