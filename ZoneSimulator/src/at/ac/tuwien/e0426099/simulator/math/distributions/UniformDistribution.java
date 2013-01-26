package at.ac.tuwien.e0426099.simulator.math.distributions;

/**
 * This is a uniform distribution, where every value has the same portability of occurrence,
 * just like the normal Random.nextDouble(); is uniformly distributed between 0.0 - 1.0
 *
 * http://en.wikipedia.org/wiki/Uniform_distribution_(discrete)
 *
 * @author PatrickF
 * @since 25.01.13
 */
public class UniformDistribution extends ADistribution {

	private Double min;
	private Double max;

	public UniformDistribution(Double min, Double max) {
		this.min = min;
		this.max = max;
	}

	public UniformDistribution(Double min, Double max, long randomSeed) {
		super(randomSeed);
		this.min = min;
		this.max = max;
	}

	@Override
	public Double getNext() {
        return min + (max - min) * getRandom().nextDouble();
	}
}
