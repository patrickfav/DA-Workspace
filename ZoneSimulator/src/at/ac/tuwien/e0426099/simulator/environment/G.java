package at.ac.tuwien.e0426099.simulator.environment;

import at.ac.tuwien.e0426099.simulator.environment.platform.Platform;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.ProcessingUnit;
import at.ac.tuwien.e0426099.simulator.environment.platform.PlatformId;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PatrickF
 * @since 18.01.13
 */
public class G {
	public static final boolean VERBOSE_LOG_MODE_GENERAL = false;
	public static final boolean VERBOSE_LOG_MODE_SUBTASK = true;
	public static final boolean VERBOSE_LOG_MODE_TASK = true;
	public static final boolean VERBOSE_LOG_MODE_SYNCTHREAD = true;
	public static final boolean VERBOSE_LOG_MODE_SLEEPTHREAD = true;
	public static final boolean VERBOSE_LOG_MODE_SCHEDULER = true;

    private Logger log = LogManager.getLogger(G.class.getName());

	private ConcurrentHashMap<UUID,Platform> platforms;

	private static G instance;

	private G() {
		platforms= new ConcurrentHashMap<UUID, Platform>();
	}

	public static G get() {
		if(instance == null) {
			instance = new G();
		}
		return instance;
	}

	/**
	 * Only for testing
	 */
	public static void recycle() {
		instance = null;
	}

	public Platform getPlatform(PlatformId id) {
		return platforms.get(id.getId());
	}

	public PlatformId addPlatform(String platformName, ProcessingUnit unit) {
		PlatformId id = new PlatformId(platformName);
		platforms.put(id.getId(),new Platform(id,unit));
		return id;
	}

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.BR+LogUtil.BR+LogUtil.h1("Complete Status:"));
        for(Platform p:platforms.values()) {
            sb.append(p.getCompleteStatus(detailed));
        }
        sb.append(LogUtil.HR1+ LogUtil.BR);
        return sb.toString();
    }

    public void start() {
        for(Platform p:platforms.values()) {
            p.start();
        }
		log.info(LogUtil.HR2);
		log.info("START");
		log.info(LogUtil.HR2);
    }

    public void pause() {
        for(Platform p:platforms.values()) {
            p.pause();
        }
		log.info(LogUtil.HR2);
		log.info("PAUSE");
    }

    public void resume() {
        for(Platform p:platforms.values()) {
            p.resumeExec();
        }
		log.info("RESUME");
		log.info(LogUtil.HR2);
    }

    public boolean waitForFinish() {
        //wait for tasks to finish
        for(Platform p:platforms.values()) {
            p.waitForFinish();
        }
        log.info(getCompleteStatus(true));

		return true;
    }
}
