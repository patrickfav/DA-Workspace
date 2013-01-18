package at.ac.tuwien.e0426099.simulator.exceptions;

/**
 * @author PatrickF
 * @since 17.01.13
 */
public class RunOnIllegalStateException extends RuntimeException {
	public RunOnIllegalStateException(String s) {
		super(s);
	}
	public RunOnIllegalStateException(Exception e) {
		super(e);
	}
	public RunOnIllegalStateException(String s, Exception e) {
		super(s,e);
	}
}
