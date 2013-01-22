package at.ac.tuwien.e0426099.simulator.environment.task.entities;

import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.RawProcessingPower;

import java.util.Date;

/**
 * @author PatrickF
 * @since 09.12.12
 */
public class ProcessingSlice {
	private Date startTime;
	private Date endTime;
	private RawProcessingPower givenProcessingPower;
	private long computationsNeededToDo;

	public ProcessingSlice(Date statTime, RawProcessingPower givenProcessingPower, long computationsNeededToDo) {
		this.startTime = statTime;
		this.givenProcessingPower = givenProcessingPower;
		this.computationsNeededToDo = computationsNeededToDo;
	}

	public ProcessingSlice(RawProcessingPower givenProcessingPower, long computationsNeededToDo) {
		this.startTime = new Date();
		this.givenProcessingPower = givenProcessingPower;
		this.computationsNeededToDo = computationsNeededToDo;
	}

	public long endProcessing() {
		endTime = new Date();
		return getActualComputationsDone();
	}

	public boolean isStillProcessing() {
		return endTime == null;
	}

	public long getEstimatedTimeInMsForFinish() {
		return givenProcessingPower.getEstimatedTimeInMsToFinish(computationsNeededToDo);
	}

	public Date getEstimatedDateForFinish() {
		if(startTime != null)
			return new Date(startTime.getTime()+givenProcessingPower.getEstimatedTimeInMsToFinish(computationsNeededToDo));

		return null;
	}

	public long getActualComputationsDone() {
		if(!isStillProcessing()) {
			return givenProcessingPower.getComputationsDone(getActualTimeSpendOnComputation());
		} else {
			return 0;
		}
	}

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

    @Override
    public String toString() {
        if(isStillProcessing()) {
            return "StartTime: "+startTime.getTime()+"/ProcPower:"+givenProcessingPower+"/CyclesTodo: "+computationsNeededToDo;
        } else  {
            return "Duration: "+getActualTimeSpendOnComputation()+" ms/ProcPower: "+givenProcessingPower+"/CyclesTodo: "+computationsNeededToDo;
        }

    }
}
