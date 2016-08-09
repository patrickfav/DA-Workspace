package at.ac.tuwien.e0426099.simulator.math.distributions;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Normal Distribution
 * http://en.wikipedia.org/wiki/Normal_distribution
 *
 * Nuff said
 *
 * @author PatrickF
 * @since 25.01.13
 */
public class NormalDistribution extends ADistribution {

	private Double mean;
	private Double standardDeviation;

	public NormalDistribution(Double mean, Double standardDeviation) {
		this.mean = mean;
		this.standardDeviation = standardDeviation;
	}

	/**
	 * Standard properties of normal distribution
	 * @param mean
	 * @param standardDeviation
	 * @param randomSeed the fixed seed for the random genrator
	 */
	public NormalDistribution(Double mean, Double standardDeviation, long randomSeed) {
        //ThreadLocalRandom.current().setSeed(randomSeed);
		this.mean = mean;
		this.standardDeviation = standardDeviation;
	}

	@Override
	public Double getNext() {
		return ThreadLocalRandom.current().nextGaussian() * standardDeviation + mean;
	}
}
