package at.ac.tuwien.e0426099.simulator.exceptions;

/**
 * @author PatrickF
 * @since 17.01.13
 */
public class CantStartException extends Exception {
	public CantStartException(String s) {
		super(s);
	}
	public CantStartException(Exception e) {
		super(e);
	}
	public CantStartException(String s, Exception e) {
		super(s,e);
	}
}
