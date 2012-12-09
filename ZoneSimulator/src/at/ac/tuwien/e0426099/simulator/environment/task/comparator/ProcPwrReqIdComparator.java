package at.ac.tuwien.e0426099.simulator.environment.task.comparator;

import at.ac.tuwien.e0426099.simulator.environment.Platform;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

import java.util.Comparator;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcPwrReqIdComparator implements Comparator<SubTaskId> {

	@Override
	public int compare(SubTaskId o1, SubTaskId o2) {
		return new Long(Platform.getInstance().getSubTask(o1).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs()).compareTo(Platform.getInstance().getSubTask(o2).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs());
	}
}
