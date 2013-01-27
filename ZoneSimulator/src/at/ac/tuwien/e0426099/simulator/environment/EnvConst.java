package at.ac.tuwien.e0426099.simulator.environment;

/**
 * Saves constants/config
 * @author PatrickF
 * @since 27.01.13
 */
public class EnvConst {

	/* log config */
	public static final boolean VERBOSE_LOG_MODE_GENERAL = true;
	public static final boolean VERBOSE_LOG_MODE_SUBTASK = true;
	public static final boolean VERBOSE_LOG_MODE_TASK = true;
	public static final boolean VERBOSE_LOG_MODE_SYNCTHREAD = true;
	public static final boolean VERBOSE_LOG_MODE_SLEEPTHREAD = true;
	public static final boolean VERBOSE_LOG_MODE_SCHEDULER = true;

	/* blocking timeout config*/
	public static final int SUBTASK_WAIT_TIMEOUT_SEC = 100;
	public static final int THREAD_BLOCKING_TIMEOUT_SEC = 100;

	/* execution factor config */
	public static final double MIN_EXEC_FACTOR = 0.1;
	public static final double MAX_EXEC_FACTOR = 20.0;
}
