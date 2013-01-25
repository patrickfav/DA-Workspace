package at.ac.tuwien.e0426099.simulator.math.graph;

import at.ac.tuwien.e0426099.simulator.math.Point;
import at.ac.tuwien.e0426099.simulator.math.PointXComparator;

import java.util.Collections;
import java.util.List;

/**
 * This is a linear graph represented by the added points.
 * An example is this: http://images.tutorvista.com/cms/images/67/double-graph.png
 *
 * It will combine all added points to a linear graph, so that getY will get y
 * abitrary x. If x is to left or to right the most left or most right point it will
 * return that extreme, making it practicaly a straigt line from -inifinte to point1
 * and from point2 to +infinite.
 *
 * @author PatrickF
 * @since 25.01.13
 */
public class LinearGraph {

	private List<Point> points;

	/**
	 * Will create the graph
	 *
	 * Will need the initial mean value e.g. the starting point
	 * @param mean is the first starting point added at x = 0
	 */
	public LinearGraph(double mean) {
		points.add(new Point(0,mean));
	}

	/**
	 * Adds a new point to the graph changing its behaviour
	 * @param p
	 */
	public void addPoint(Point p) {
		points.add(p);
		Collections.sort(points,new PointXComparator());
	}

	/**
	 * Get y to given x. There is no restriction on the range, other than it must be in double-range
	 * @param x
	 * @return
	 */
	public double getY(double x) {
		if(!points.isEmpty()) {
			if(x <= points.get(0).getX()) { //if there is no point beyond smallest, return min
				return points.get(0).getY();
			} else if(x >= points.get(points.size()).getX()) { //if there is no point beyond smallest, return max
				return  points.get(points.size()).getY();
			} else {
				for(int i=0;i<points.size();i++) {
					if(points.get(i).getX() >= x) { //got the point right to x, so take this and the next left
						return getY(points.get(i - 1), points.get(i), x); //i-1 should be ok, since it would have been chaught by th ifs
					}
				}
			}
		}
		return 0;
	}

	/**
	 * Computes a straight line from point p1 to point p2 and will return x to given x on this line
	 * See: http://demo.activemath.org/ActiveMath2/search/show.cmd?id=mbase://AC_UK_calculus/functions/ex_linear_equation_two_points
	 *
	 * @param p1
	 * @param p2
	 * @param x
	 * @return
	 */
	private double getY(Point p1, Point p2, double x) {
		return p1.getY()+((p2.getY()-p1.getY()) /(p2.getX()-p1.getX())) * (x - p1.getX());
	}

}
