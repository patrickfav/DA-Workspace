package at.ac.tuwien.e0426099.simulator.environment;

import at.ac.tuwien.e0426099.simulator.environment.zone.Zone;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.ProcessingUnit;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.helper.util.LogUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the God class - a singelton managing all zones
 *
 * @author PatrickF
 * @since 18.01.13
 */
public class Env {

	private Logger log = LogManager.getLogger(Env.class.getName());

	private ConcurrentHashMap<UUID,Zone> zones;
	private double executionFactor; //how fast the system runs e.g x1 is normal x2 is twice as fast

	private static Env instance;

	private Env() {
		zones = new ConcurrentHashMap<UUID, Zone>();
		executionFactor=1.0;
	}

	public static Env get() {
		if(instance == null) {
			instance = new Env();
		}
		return instance;
	}

	/**
	 * Only for testing
	 */
	public static void recycle() {
		instance = null;
	}

	public Zone getZone(ZoneId id) {
		return zones.get(id.getId());
	}

	public ZoneId addZone(String platformName, ProcessingUnit unit) {
		ZoneId id = new ZoneId(platformName);
		zones.put(id.getId(), new Zone(id, unit));
		return id;
	}

    public synchronized String getCompleteStatus(boolean detailed) {
        StringBuffer sb = new StringBuffer();
        sb.append(LogUtil.BR+LogUtil.BR+LogUtil.h1("Complete Status:"));
        for(Zone p: zones.values()) {
            sb.append(p.getCompleteStatus(detailed));
        }
        sb.append(LogUtil.HR1+ LogUtil.BR);
        return sb.toString();
    }

	/**
	 * Starts with execition factor (e.g. how fast the system runs, 1.0 is normal, 2.0 is twice as fast)
	 *
	 * @param executionFactor between MIN_EXEC_FACTOR and MAX_EXEC_FACTOR
	 */
	public void start(double executionFactor) {
		this.executionFactor = Math.min(EnvConst.MAX_EXEC_FACTOR,Math.max(EnvConst.MIN_EXEC_FACTOR,executionFactor));

		log.info(LogUtil.BR+LogUtil.HR2+LogUtil.BR+"START x"+executionFactor+LogUtil.BR+LogUtil.HR2);

		for(Zone p: zones.values()) {
			p.setExecutionFactor(executionFactor);
			p.start();
		}
	}

    public void start() {
    	start(1.0);
    }

    public void pause() {
		log.info(LogUtil.BR+LogUtil.HR2+LogUtil.BR+"PAUSE"+LogUtil.BR);

        for(Zone p: zones.values()) {
            p.pause();
        }

    }

	public void resume() {
		resume(executionFactor);
	}

    public void resume(double executionFactor) {
		this.executionFactor = Math.min(EnvConst.MAX_EXEC_FACTOR,Math.max(EnvConst.MIN_EXEC_FACTOR,executionFactor));

		log.info(LogUtil.BR+"RESUME x"+executionFactor+LogUtil.BR+LogUtil.HR2);

        for(Zone p: zones.values()) {
			p.setExecutionFactor(executionFactor);
            p.resumeExec();
        }
    }

    public boolean waitForFinish() {
        //wait for tasks to finish
        for(Zone p: zones.values()) {
            p.waitForFinish();
        }
        log.info(getCompleteStatus(true));

		return true;
    }
}
