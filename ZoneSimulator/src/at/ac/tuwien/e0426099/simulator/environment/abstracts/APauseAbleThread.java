package at.ac.tuwien.e0426099.simulator.environment.abstracts;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Created with IntelliJ IDEA.
 * User: PatrickF
 * Date: 21.01.13
 * Time: 22:39
 * To change this template use File | Settings | File Templates.
 */
public abstract class APauseAbleThread<T> extends Thread {
    private final Logger log = LogManager.getLogger(this.getClass().getName());

    private BlockingQueue<T> workerQueue;
    private Semaphore pauseSemaphore;
    private boolean isOnPause;
	private boolean workSwitch;

    public APauseAbleThread() {
        workerQueue = new LinkedBlockingQueue<T>();
        pauseSemaphore=new Semaphore(0,false);
		workSwitch=true;
    }

    @Override
    public void run() {
        while (checkIfThereWillBeAnyWork()) {
            if (isOnPause) {
                log.debug(this + " [Sync] waiting in pause mode");
                pauseSemaphore.acquireUninterruptibly();
                log.debug(this + " [Sync] resuming");
            }
            log.debug(this + " [Sync] Waiting for dispatching next task");
            try {doTheWork(workerQueue.take());} catch (InterruptedException e) {/*ignore*/}
        }
		onAllDone();
        log.debug(this + " All done.");
    }

    public void pause() {
		log.debug(this + " [Sync] pause called");
        isOnPause = true;
    }

    public void resumeExec() {
		log.debug(this + " [Sync] resume called");
        if(isOnPause) {
            isOnPause = false;
            pauseSemaphore.release();
        } else {
            log.warn("Trying to resume, but not in pause.");
        }
    }

	public void stopExec() {
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
		log.debug(this+ " [Sync] wait for task to finish");
		try {
			join();
		} catch (InterruptedException e) {
			//ignore
		}
	}
}
