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

	/**
	 * Returns the difference of actual to base in percentage.
	 * E.g. base is 90 and actual is 120 this will return +33,333(%)
	 * @param actual
	 * @param base
	 * @return
	 */
	public static double getPrecentageDifference(double actual, double base) {
		return ((actual/base) -1) *100;
	}

	/**
	 * Returns the input unless its smaller or bigger than min/max respectivly.
	 * In that case it will return min/max
	 * @param input
	 * @param min
	 * @param max
	 * @return
	 */
	public static double inRange(double input, double min, double max) {
		return Math.min(max,Math.max(min,input));
	}
}
