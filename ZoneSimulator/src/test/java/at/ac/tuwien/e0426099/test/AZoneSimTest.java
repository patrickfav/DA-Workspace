package at.ac.tuwien.e0426099.test;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Random;

/**
 * @author PatrickF
 * @since 23.01.13
 */
public abstract class AZoneSimTest {
    public final static long SEED = 794380265;
    protected Logger log = LogManager.getLogger(this.getClass().getName());

	private Random fixedRandom = new Random(SEED);
	private Random realPseudoRandom = new Random();


	protected long getFixedLongInRange(long min, long max) {
		return min + (int)(fixedRandom.nextDouble() * ((max - min) + 1));
	}

	protected long getFixedLongGaussian(long mean, long std_deviance) {
		return (long) fixedRandom.nextGaussian() * std_deviance + mean;
	}

	protected void sleepUninterruptly(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
