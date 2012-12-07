package at.ac.tuwien.e0426099.simulator.exceptions;

/**
 * @author PatrickF
 * @since 07.12.12
 */
public class TooMuchConcurrentTasks extends Exception{
	public TooMuchConcurrentTasks(String s) {
		super(s);
	}
	public TooMuchConcurrentTasks(Exception e) {
		super(e);
	}
	public TooMuchConcurrentTasks(String s, Exception e) {
		super(s,e);
	}
}
