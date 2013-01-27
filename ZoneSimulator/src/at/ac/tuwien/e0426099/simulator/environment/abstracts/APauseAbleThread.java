package at.ac.tuwien.e0426099.simulator.environment.abstracts;

import at.ac.tuwien.e0426099.simulator.environment.EnvConst;
import at.ac.tuwien.e0426099.simulator.helper.Log;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a abstract for the main functions of the pause and run functionality
 */
public abstract class APauseAbleThread<T> extends Thread {
	private Log log = new Log(this, EnvConst.VERBOSE_LOG_MODE_GENERAL && EnvConst.VERBOSE_LOG_MODE_SYNCTHREAD);
    private BlockingDeque<T> workerQueue;
    private Semaphore pauseSemaphore;
    private volatile boolean isOnPause;
	private boolean workSwitch;
	private Lock workLock;
	private double executionFactor;

    public APauseAbleThread() {
        workerQueue = new LinkedBlockingDeque<T>();
        pauseSemaphore=new Semaphore(0,false);
		workSwitch=true;
		workLock = new ReentrantLock(true);
		executionFactor=1.0;
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
				T obj = workerQueue.poll(EnvConst.THREAD_BLOCKING_TIMEOUT_SEC, TimeUnit.SECONDS);
				if(obj != null) {
					doTheWork(obj);
				} else {
					doTheWork(null);
					log.d("[Sync] Timeout");
				}
			} catch (InterruptedException e) {
				doTheWork(null);
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

    protected void addToWorkerQueue(T obj) {
		try {
			workerQueue.putLast(obj);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected boolean checkIfThereWillBeAnyWork() {
		return workSwitch;
	}

	public void waitForFinish() {
		log.d("[Sync] wait for task to finish");
		try {
			join();
		} catch (InterruptedException e) {
			log.w("[Sync] interrupt called while waitForFinish");
		}
	}

	protected Log getLog() {
		return log;
	}

	public Lock getWorkLock() {
		return workLock;
	}

	public double getExecutionFactor() {
		return executionFactor;
	}

	public void setExecutionFactor(double executionFactor) {
		this.executionFactor =  executionFactor;
	}

	protected abstract void doTheWork(T input);
	protected abstract void onAllDone();
}
