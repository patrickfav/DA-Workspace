package at.ac.tuwien.e0426099.simulator.helper;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author PatrickF
 * @since 23.01.13
 */
public class Log {
	private Logger log;
	private boolean showVerbose;
    private String prefix;

	private Object obj;

	public Log(Object obj, boolean showVerbose) {
        log = LogManager.getLogger(obj.getClass().getSimpleName());
		this.showVerbose =showVerbose;

		try {
			setLogTag(obj,obj.toString());
		} catch(NullPointerException e) {
			setLogTag(obj,null);
		}
	}

	public void setLogTag(Object obj, String tag) {
		this.obj =obj;

        if(tag != null && !tag.equals("")) {
            prefix = tag+": ";
        } else {
            prefix="";
        }
	}

	public void refreshData(){
        try {
            setLogTag(obj,obj.toString());
        } catch(NullPointerException e) {
            setLogTag(obj,null);
        }
	}

	public void i(String msg) {
		log.info(prefix+msg);
	}
	public void d(String msg) {
		log.debug(prefix+msg);
	}
	public void v(String msg) {
		if(showVerbose)
			log.trace(prefix+msg);
	}
	public void w(String msg) {
		log.warn(prefix+msg);
	}
	public void w(String msg, Exception e) {
		log.warn(prefix+msg,e);
	}
	public void e(String msg) {
		log.error(prefix+msg);
	}
	public void e(String msg, Exception e) {
		log.error(prefix+msg,e);
	}
}
