package at.ac.tuwien.e0426099.simulator.math.distributions;

import java.util.Random;

/**
 * @author PatrickF
 * @since 25.01.13
 */
public abstract class ADistribution implements IRandomDistribution{
	private Random random;

	/**
	 * Constructor with pseudo random seed
	 */
	protected ADistribution() {
		this.random = new Random();
	}

	/**
	 * A constructor with fixed random seed
	 * @param seedForRandom
	 */
	protected ADistribution(long seedForRandom) {
		this.random = new Random(seedForRandom);
	}

	@Override
	public abstract Double getNext();

	public Random getRandom() {
		return random;
	}
}
