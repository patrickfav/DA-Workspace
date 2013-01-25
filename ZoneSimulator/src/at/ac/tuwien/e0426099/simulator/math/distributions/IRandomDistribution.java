package at.ac.tuwien.e0426099.simulator.math.distributions;

/**
 * This interface is a simple wrapper for different Random methods (like normal distribution).
 * Its supposed to wrap all the needed factors, like mean or standard-deviance to get a specific
 * statistical distribution (not only the general model)
 * @author PatrickF
 * @since 25.01.13
 */
public interface IRandomDistribution {

	/**
	 * Gets a pseudo random double for this model of distribution
	 * @return
	 */
	public Double getNext();
}
