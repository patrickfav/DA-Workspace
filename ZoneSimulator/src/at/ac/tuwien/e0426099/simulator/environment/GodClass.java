package at.ac.tuwien.e0426099.simulator.environment;

import at.ac.tuwien.e0426099.simulator.environment.processor.ProcessingUnit;
import at.ac.tuwien.e0426099.simulator.util.LogUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PatrickF
 * @since 18.01.13
 */
public class GodClass {
	public static final boolean VERBOSE_LOG_MODE = true;

	private ConcurrentHashMap<UUID,Platform> platforms;

	private static GodClass instance;

	private GodClass() {
		platforms= new ConcurrentHashMap<UUID, Platform>();
	}

	public static GodClass instance() {
		if(instance == null) {
			instance = new GodClass();
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
}
