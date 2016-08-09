package at.ac.tuwien.e0426099.simulator.exceptions;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class TooMuchConcurrentTasksException extends Exception{
	public TooMuchConcurrentTasksException(String s) {
		super(s);
	}
	public TooMuchConcurrentTasksException(Exception e) {
		super(e);
	}
	public TooMuchConcurrentTasksException(String s, Exception e) {
		super(s,e);
	}
}
