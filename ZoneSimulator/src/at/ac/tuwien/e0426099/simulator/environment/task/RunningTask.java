package at.ac.tuwien.e0426099.simulator.environment.task;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author PatrickF
 * @since 07.12.12
 */
public class RunningTask extends Thread{
	private Logger log = LogManager.getLogger(RunningTask.class.getName());

	private long ramNeedsMiB;
	private long amountToCompute;
	private long amountToComputeLeft;
	private long computePerSecond;

	private List<Long> startTimes;

	public RunningTask(long amountToCompute, long computePerSecond) {
		this.amountToCompute=amountToComputeLeft=amountToCompute;
		this.computePerSecond=computePerSecond;
		startTimes = new ArrayList<Long>();
	}

	@Override
	public void run() {
		try {
			log.debug("Start");
			startTimes.add(new Date().getTime());
			sleep(calculateComputingTime(amountToComputeLeft, computePerSecond));

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void interrupt() {
		super.interrupt();
	}


	private long calculateComputingTime(long amountToCompute, long computePerSecond) {
		return (long) Math.ceil(amountToCompute/computePerSecond);
	}
}
