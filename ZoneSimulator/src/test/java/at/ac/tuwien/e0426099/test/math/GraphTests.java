package at.ac.tuwien.e0426099.test.math;

import at.ac.tuwien.e0426099.simulator.math.Point;
import at.ac.tuwien.e0426099.simulator.math.graph.LinearGraph;
import at.ac.tuwien.e0426099.test.AZoneSimTest;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: PatrickF
 * Date: 26.01.13
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class GraphTests extends AZoneSimTest {
    public static final int MEAN = 10;

    @Test
    public void testLinearGraphOnlyMean() {
        LinearGraph graph = new LinearGraph(MEAN);

        for (int i = 0; i < 6000; i++) {
            log.debug("(" + (new Double(i) / 100.0) + "," + graph.getY(new Double(i) / 100.0) + ")");
            assertTrue(graph.getY(new Double(i) / 100.0) == MEAN);
        }
    }

    @Test
    public void testLinearGraph1Point() {
        LinearGraph graph = new LinearGraph(MEAN);
        graph.addPoint(new Point(30, 20));
        for (int i = 0; i < 6000; i++) {
            log.debug("(" + (new Double(i) / 100.0) + "," + graph.getY(new Double(i) / 100.0) + ")");

            if (i <= 0) {
                assertTrue(graph.getY(new Double(i) / 100.0) == MEAN);
            } else if (i > 0 && i < 3000) {
                assertTrue(graph.getY(new Double(i) / 100.0) > MEAN && graph.getY(new Double(i) / 100.0) < 20);
            } else if (i >= 3000)
                assertTrue(graph.getY(new Double(i) / 100.0) == 20);
        }
    }

    @Test
    public void testLinearGraph2Points() {
        LinearGraph graph = new LinearGraph(MEAN);
        graph.addPoint(new Point(40, 20));
        graph.addPoint(new Point(10, 5));
        for (int i = 0; i < 6000; i++) {
            log.debug("(" + (new Double(i) / 100.0) + "," + graph.getY(new Double(i) / 100.0) + ")");

            if (i <= 0) {
                assertTrue(graph.getY(new Double(i) / 100.0) == MEAN);
            } else if (i > 0 && i < 1000) {
                assertTrue(graph.getY(new Double(i) / 100.0) < MEAN && graph.getY(new Double(i) / 100.0) > 5);
            }  else if (i > 1000 && i < 4000) {
                assertTrue(graph.getY(new Double(i) / 100.0) > 5 && graph.getY(new Double(i) / 100.0) < 20);
            } else if (i >= 4000)
                assertTrue(graph.getY(new Double(i) / 100.0) == 20);
        }
    }

    @Test
    public void testLinearGraph10Points() {
        LinearGraph graph = new LinearGraph(MEAN);
        graph.addPoint(new Point(1, 5));
        graph.addPoint(new Point(5, 15));
        graph.addPoint(new Point(10, 50));
        graph.addPoint(new Point(15, 2));
        graph.addPoint(new Point(20, 50));
        graph.addPoint(new Point(25, 51));
        graph.addPoint(new Point(30, 52));
        graph.addPoint(new Point(40, 16));
        graph.addPoint(new Point(41, 4));
        graph.addPoint(new Point(42, 20));
        graph.addPoint(new Point(54, 100));

        for (int i = 0; i < 6000; i++) {
            log.debug("(" + (new Double(i) / 100.0) + "," + graph.getY(new Double(i) / 100.0) + ")");

            if (i <= 0) {
                assertTrue(graph.getY(new Double(i) / 100.0) == MEAN);
            } else if (i >= 5400)
                assertTrue(graph.getY(new Double(i) / 100.0) == 100);
        }
    }

}
