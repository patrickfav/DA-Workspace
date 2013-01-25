package at.ac.tuwien.e0426099.simulator.math;

import java.util.Comparator;

/**
 * Compares with property X on a point
 * @author PatrickF
 * @since 25.01.13
 */
public class PointXComparator implements Comparator<Point> {

	@Override
	public int compare(Point o1, Point o2) {
		return (new Double(o1.getX())).compareTo(o2.getX());  //To change body of implemented methods use File | Settings | File Templates.
	}
}
