package at.ac.tuwien.e0426099.simulator.math;

/**
 * Simple point representation
 *
 * awt.Point had only int, but I needed double precision
 * This implementation is immutable.
 * @author PatrickF
 * @since 25.01.13
 */
public class Point {
	private double x;
	private double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
}
