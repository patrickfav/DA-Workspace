package at.ac.tuwien.e0426099.simulator.environment.task.entities;

import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.RawProcessingPower;

import java.util.Date;

/**
 * Represents a working intervall
 *
 * @author PatrickF
 * @since 09.12.12
 */
public class ProcessingSlice {
	private Date startTime;
	private Date endTime;
	private RawProcessingPower givenProcessingPower;
	private long computationsNeededToDo;
	private double executionFactor;

	public ProcessingSlice(Date statTime, RawProcessingPower givenProcessingPower, long computationsNeededToDo, double executionFactor) {
		this.startTime = statTime;
		this.givenProcessingPower = givenProcessingPower;
		this.computationsNeededToDo = computationsNeededToDo;
		this.executionFactor = executionFactor;
	}

	public ProcessingSlice(RawProcessingPower givenProcessingPower, long computationsNeededToDo, double executionFactor) {
		this(new Date(),givenProcessingPower,computationsNeededToDo,executionFactor);
	}

	public long endProcessing() {
		endTime = new Date();
		return getActualComputationsDone();
	}

	public boolean isStillProcessing() {
		return endTime == null;
	}

	public long getEstimatedTimeInMsForFinish() {
		return givenProcessingPower.getEstimatedTimeInMsToFinish((long) Math.ceil(((double)computationsNeededToDo) /executionFactor));
	}

	public Date getEstimatedDateForFinish() {
		if(startTime != null)
			return new Date(startTime.getTime()+getEstimatedTimeInMsForFinish());

		return null;
	}

	/**
	 * This will return the computations that has been done in this interval.
	 * If still in progress will return 0
	 * @return
	 */
	public long getActualComputationsDone() {
		if(!isStillProcessing()) {
			return givenProcessingPower.getComputationsDone(Math.round(((double) getActualTimeSpendOnComputation())*executionFactor));
		} else {
			return 0;
		}
	}

	/**
	 * Return ms duration for this interval
	 * @return
	 */
	public long getActualTimeSpendOnComputation() {
		if(startTime != null && !isStillProcessing()) {
			return endTime.getTime() - startTime.getTime();
		} else {
			return 0;
		}
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public double getExecutionFactor() {
		return executionFactor;
	}

	@Override
    public String toString() {
        if(isStillProcessing()) {
            return "StartTime: "+startTime.getTime()+"/ProcPower:"+givenProcessingPower+"/CyclesTodo: "+computationsNeededToDo +"/Factor x"+executionFactor;
        } else  {
            return "Duration: "+getActualTimeSpendOnComputation()+" ms/ProcPower: "+givenProcessingPower+"/CyclesTodo: "+computationsNeededToDo+"/Factor x"+executionFactor;
        }

    }
}
