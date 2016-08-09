package at.ac.tuwien.e0426099.test.math;

import at.ac.tuwien.e0426099.simulator.math.distributions.FixedNumberDistribution;
import at.ac.tuwien.e0426099.simulator.math.distributions.IRandomDistribution;
import at.ac.tuwien.e0426099.simulator.math.distributions.NormalDistribution;
import at.ac.tuwien.e0426099.simulator.math.distributions.UniformDistribution;
import at.ac.tuwien.e0426099.test.AZoneSimTest;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * User: PatrickF
 * Date: 26.01.13
 * Time: 13:21
 */
public class DistributionTests extends AZoneSimTest{
    public static final int TEST_CYCLES = 1000;

    public static final double START_RANGE = 0.0;
    public static final double END_RANGE = 10.0;

    public static final double MEAN = 0.0;
    public static final double STD_DEV = 10.0;

    @Test
    public void testUniformDistributionDouble() {
        IRandomDistribution distribution = new UniformDistribution(START_RANGE,END_RANGE,SEED);

        double next;
        for(int i=0;i<TEST_CYCLES;i++) {
            next = distribution.getNext();
            log.debug("Next (uniform): "+next);
            assertTrue((START_RANGE <= next) && (next <= END_RANGE));
        }
    }

    @Test
    public void testUniformDistributionLong() {
        IRandomDistribution distribution = new UniformDistribution(START_RANGE,END_RANGE,SEED);

        double next;
        for(int i=0;i<TEST_CYCLES;i++) {
            next = distribution.getNextLong();
            log.debug("Next (uniform): "+next);
            assertTrue((START_RANGE <= next) && (next <= END_RANGE));
        }
    }

    @Test
    public void testNormalDistributionDouble() {
        IRandomDistribution distribution = new NormalDistribution(MEAN,STD_DEV,SEED);

        double next;
        for(int i=0;i<TEST_CYCLES;i++) {
            next = distribution.getNext();
            log.debug("Next (normal): "+next);
            assertTrue(((MEAN - 4 * STD_DEV) <= next) && (next <= (MEAN + 4 * STD_DEV)));
        }
    }

    @Test
    public void testNormalDistributionLong() {
        IRandomDistribution distribution = new NormalDistribution(MEAN,STD_DEV,SEED);

        double next;
        for(int i=0;i<TEST_CYCLES;i++) {
            next = distribution.getNextLong();
            log.debug("Next (normal): "+next);
            assertTrue(((MEAN - 4 * STD_DEV) <= next) && (next <= (MEAN + 4 * STD_DEV)));
        }
    }

    @Test
    public void testFixedDistributionDouble() {
        IRandomDistribution distribution = new FixedNumberDistribution(END_RANGE);

        double next;
        for(int i=0;i<TEST_CYCLES;i++) {
            next = distribution.getNext();
            log.debug("Next (fixed): "+next);
            assertTrue(next == END_RANGE);
        }
    }

    @Test
    public void testFixedDistributionLong() {
        IRandomDistribution distribution = new FixedNumberDistribution(END_RANGE);

        double next;
        for(int i=0;i<TEST_CYCLES;i++) {
            next = distribution.getNextLong();
            log.debug("Next (fixed): "+next);
            assertTrue(next == END_RANGE);
        }
    }
}
