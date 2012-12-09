package at.ac.tuwien.e0426099.simulator.environment.task.comparator;

import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.IComputationalSubTask;

import java.util.Comparator;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcPwrReqComparator implements Comparator<IComputationalSubTask> {

	@Override
	public int compare(IComputationalSubTask o1, IComputationalSubTask o2) {
		return new Long(o1.getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs()).compareTo(o2.getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs());
	}
}
