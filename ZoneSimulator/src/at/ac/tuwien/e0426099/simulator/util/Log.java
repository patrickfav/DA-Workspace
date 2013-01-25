package at.ac.tuwien.e0426099.simulator.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author PatrickF
 * @since 23.01.13
 */
public class Log {
	private Logger log;
	private boolean showVerbose;

	private Object obj;

	public Log(Object obj, boolean showVerbose) {
		this.showVerbose =showVerbose;
		try {
			setLogTag(obj,obj.toString());
		} catch(NullPointerException e) {
			setLogTag(obj,obj.getClass().getSimpleName());
		}
	}

	public void setLogTag(Object obj, String tag) {
		this.obj =obj;
		log = LogManager.getLogger(tag);
	}

	public void refreshData(){
		setLogTag(obj,obj.toString());
	}

	public void i(String msg) {
		log.info(msg);
	}
	public void d(String msg) {
		log.debug(msg);
	}
	public void v(String msg) {
		if(showVerbose)
			log.trace(msg);
	}
	public void w(String msg) {
		log.warn(msg);
	}
	public void w(String msg, Exception e) {
		log.warn(msg,e);
	}
	public void e(String msg) {
		log.error(msg);
	}
	public void e(String msg, Exception e) {
		log.error(msg,e);
	}
}
