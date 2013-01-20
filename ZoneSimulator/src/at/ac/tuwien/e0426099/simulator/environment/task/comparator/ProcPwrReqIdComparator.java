package at.ac.tuwien.e0426099.simulator.environment.task.comparator;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

import java.util.Comparator;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcPwrReqIdComparator implements Comparator<SubTaskId> {
	private PlatformId platformId;

	public ProcPwrReqIdComparator(PlatformId platformId) {
		this.platformId = platformId;
	}

	@Override
	public int compare(SubTaskId o1, SubTaskId o2) {
		return new Long(G.get().getPlatform(platformId).getSubTaskForProcessor(o1).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs())
				.compareTo(G.get().getPlatform(platformId).getSubTaskForProcessor(o2).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs());
	}
}
