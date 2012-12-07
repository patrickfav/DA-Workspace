package at.ac.tuwien.e0426099.simulator.environment.task.comparator;

import at.ac.tuwien.e0426099.simulator.environment.task.IRunnableTask;

import java.util.Comparator;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcPwrReqComparator implements Comparator<IRunnableTask> {

	@Override
	public int compare(IRunnableTask o1, IRunnableTask o2) {
		return o1.getProcessingRequirements().getMaxComputationalUtilization().compareTo(o2.getProcessingRequirements().getMaxComputationalUtilization());
	}
}
