package at.ac.tuwien.e0426099.simulator.environment;

import at.ac.tuwien.e0426099.simulator.environment.processor.ProcessingUnit;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * @author PatrickF
 * @since 18.01.13
 */
public class G {
	public static final boolean VERBOSE_LOG_MODE = true;
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
    }

    public void pause() {
        for(Platform p:platforms.values()) {
            p.pause();
        }
    }

    public void resume() {
        for(Platform p:platforms.values()) {
            p.resumeExec();
        }
    }

    public void waitForFinish() {
        //wait for tasks to finish
        for(Platform p:platforms.values()) {
            try { p.join();} catch (InterruptedException e) {e.printStackTrace();}
        }
        log.info(getCompleteStatus(true));
    }
}
