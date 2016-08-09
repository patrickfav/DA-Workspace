package at.ac.tuwien.e0426099.simulator.math.distributions;

/**
 * No Randomness, returns a fixed double every time
 *
 * @author PatrickF
 * @since 25.01.13
 */
public class FixedNumberDistribution extends ADistribution {

	private Double number;

	public FixedNumberDistribution(Double number) {
		this.number = number;
	}

	@Override
	public Double getNext() {
		return number;
	}
}
