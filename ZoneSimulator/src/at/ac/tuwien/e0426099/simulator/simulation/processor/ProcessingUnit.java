package at.ac.tuwien.e0426099.simulator.simulation.processor;

import at.ac.tuwien.e0426099.simulator.simulation.task.IRunnableTask;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class ProcessingUnit  implements ProcessingCore.ProcessingUnitListener {

	private List<ProcessingCore> cores;
	private Queue<IRunnableTask> taskQueue;

	public ProcessingUnit() {
		taskQueue=new ConcurrentLinkedQueue<IRunnableTask>();
	}

	public void addTask() {

	}

	@Override
	public void onTaskFinished(ProcessingCore c, IRunnableTask t) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
