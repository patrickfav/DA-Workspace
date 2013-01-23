package at.ac.tuwien.e0426099.simulator.environment.abstracts;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.util.Log;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: PatrickF
 * Date: 21.01.13
 * Time: 22:39
 * To change this template use File | Settings | File Templates.
 */
public abstract class APauseAbleThread<T> extends Thread {
	private Log log = new Log(this, G.VERBOSE_LOG_MODE_GENERAL && G.VERBOSE_LOG_MODE_SYNCTHREAD);
	private final static long BLOCKING_TIMEOUT_SEC = 15;
    private BlockingDeque<T> workerQueue;
    private Semaphore pauseSemaphore;
    private volatile boolean isOnPause;
	private boolean workSwitch;

    public APauseAbleThread() {
        workerQueue = new LinkedBlockingDeque<T>();
        pauseSemaphore=new Semaphore(0,false);
		workSwitch=true;
    }

    @Override
    public void run() {
        while (checkIfThereWillBeAnyWork()) {
            if (isOnPause) {
                log.d("[Sync] waiting in pause mode");
                pauseSemaphore.acquireUninterruptibly();
                log.d("[Sync] resuming");
            }
            log.d("[Sync] Waiting for dispatching next task");
			try {
				T obj = workerQueue.poll(BLOCKING_TIMEOUT_SEC, TimeUnit.SECONDS);
				if(obj != null) {
					doTheWork(obj);
				} else {
					log.d("[Sync] Timeout");
				}
			} catch (InterruptedException e) {
				log.w("[Sync] interrupt called while waiting: "+e);
			}
        }
		onAllDone();
        log.d("All done.");
    }

    public synchronized void pause() {
		log.d("[Sync] pause called");
        isOnPause = true;
    }

    public synchronized void resumeExec() {
		log.d("[Sync] resume called");
        if(isOnPause) {
            isOnPause = false;
            pauseSemaphore.release();
        } else {
            log.w("Trying to resume, but not in pause.");
        }
    }

	public synchronized void stopExec() {
		workSwitch = false;
	}

    public void addToWorkerQueue(T obj) {
        workerQueue.add(obj);
    }

    public boolean checkIfThereWillBeAnyWork() {
		return workSwitch;
	}
    public abstract void doTheWork(T input);
	public abstract void onAllDone();

	public void waitForFinish() {
		log.d("[Sync] wait for task to finish");
		try {
			join();
		} catch (InterruptedException e) {
			log.w("[Sync] interrupt called while waitForFinish");
		}
	}

	public Log getLog() {
		return log;
	}
}
