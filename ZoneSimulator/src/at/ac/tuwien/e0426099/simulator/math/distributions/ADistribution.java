package at.ac.tuwien.e0426099.simulator.math.distributions;

import java.util.Random;

/**
 * @author PatrickF
 * @since 25.01.13
 */
public abstract class ADistribution implements IRandomDistribution{
	@Override
	public abstract Double getNext();

    @Override
    public Long getNextLong() {
        return (long) Math.ceil(getNext());
    }
}
