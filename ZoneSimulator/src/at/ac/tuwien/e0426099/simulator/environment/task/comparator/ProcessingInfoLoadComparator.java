package at.ac.tuwien.e0426099.simulator.environment.task.comparator;

import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.ProcessingCoreInfo;

import java.util.Comparator;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingInfoLoadComparator implements Comparator<ProcessingCoreInfo> {

	@Override
	public int compare(ProcessingCoreInfo o1, ProcessingCoreInfo o2) {
		return o2.getLoad().compareTo(o1.getLoad());
	}
}
