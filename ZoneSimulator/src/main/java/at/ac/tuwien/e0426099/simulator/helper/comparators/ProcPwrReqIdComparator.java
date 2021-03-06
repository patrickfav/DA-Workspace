package at.ac.tuwien.e0426099.simulator.helper.comparators;

import at.ac.tuwien.e0426099.simulator.environment.Env;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

import java.util.Comparator;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcPwrReqIdComparator implements Comparator<SubTaskId> {
	private ZoneId zoneId;

	public ProcPwrReqIdComparator(ZoneId zoneId) {
		this.zoneId = zoneId;
	}

	@Override
	public int compare(SubTaskId o1, SubTaskId o2) {
		return new Long(Env.get().getZone(zoneId).getSubTaskForProcessor(o1).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs())
				.compareTo(Env.get().getZone(zoneId).getSubTaskForProcessor(o2).getProcessingRequirements().getMaxComputationalUtilization().getComputationsPerMs());
	}
}
