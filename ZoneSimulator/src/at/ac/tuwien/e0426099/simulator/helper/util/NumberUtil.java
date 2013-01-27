package at.ac.tuwien.e0426099.simulator.helper.util;

/**
 * @author PatrickF
 * @since 27.01.13
 */
public class NumberUtil {

	/**
	 * Round to the given precision
	 * If number is 3.14565 and precision is 2, then
	 * output will be 3.15
	 *
	 * @param precision
	 * @return
	 */
	public static double round(double number, int precision) {
		return Math.round(number * Math.pow(10.0,(double) precision)) /  Math.pow(10.0,(double) precision);
	}
}
