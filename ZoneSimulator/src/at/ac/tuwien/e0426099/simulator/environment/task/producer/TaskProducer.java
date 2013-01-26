package at.ac.tuwien.e0426099.simulator.environment.task.producer;

import at.ac.tuwien.e0426099.simulator.environment.Env;
import at.ac.tuwien.e0426099.simulator.environment.task.ComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.Task;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import at.ac.tuwien.e0426099.simulator.environment.task.producer.templates.ComputationalSubTaskTemplate;
import at.ac.tuwien.e0426099.simulator.environment.task.producer.templates.ISubTaskTemplate;
import at.ac.tuwien.e0426099.simulator.environment.task.producer.templates.TaskTemplate;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.math.Point;
import at.ac.tuwien.e0426099.simulator.math.graph.LinearGraph;
import at.ac.tuwien.e0426099.simulator.helper.util.DateUtil;
import at.ac.tuwien.e0426099.simulator.helper.Log;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author PatrickF
 * @since 25.01.13
 */
public class TaskProducer extends Thread{
	private Log log = new Log(this, Env.VERBOSE_LOG_MODE_GENERAL);
	private static final int FREQUENCY_MS = 150; //how long a cycle is

	private Random random;
	private LinearGraph timeUsageGarph;
	private TaskTemplate template;
	private Queue<ZoneId> zoneIds;
	private boolean running;
    private Date startTime;

	public TaskProducer(TaskTemplate template, double avgCallsPerSec) {
		this.template = template;
		timeUsageGarph = new LinearGraph(avgCallsPerSec);
		running=true;
		zoneIds=new ConcurrentLinkedQueue<ZoneId>();
		random = new Random();
        startTime=new Date();
		log.refreshData();
	}

	/**
	 * Adds a point in the linear time-usage-graph. In this case
	 * a full usage cycle is not a day, but an hour. This is to
	 * change usage in at differnet times (like in the morning
	 * is usage smaller than in the evening)
	 *
	 * @param minute 0-59
	 * @param callsPerSec
	 */
	public void addUsagePoint(int minute,double callsPerSec) {
		timeUsageGarph.addPoint(new Point(Math.min(59, Math.max(0, minute)),callsPerSec));
	}

	public void setZoneId(ZoneId zoneId) {
		zoneIds.add(zoneId);
		log.refreshData();
	}

	@Override
	public void run() {
		log.i("Start producer");
		while(running) {
			try {
				log.v("start new cycle");
				sleep(FREQUENCY_MS);
				computeCountAndRunTaskForThisCycle();
			} catch (InterruptedException e) {
				log.w("Interrupt called "+e);
			}
		}
		log.i("All done.");
	}

	public void stopExec() {
		running = false;
		interrupt();
	}

	/**
	 * Gets the current usage and coverts it to frequency and adds task accordingly
	 */
	private void computeCountAndRunTaskForThisCycle() {
        Calendar c= new GregorianCalendar();
        c.setTimeInMillis(DateUtil.elapsedTime(startTime,new Date())); //starting time as a period from startup of this producer (e.g always start at time 0, not an arbitrary time)
		double currentUsage = timeUsageGarph.getY(Calendar.getInstance().get(Calendar.MINUTE)+(Calendar.getInstance().get(Calendar.SECOND)/60));
		log.v("Current usage per sec "+currentUsage+".");

		currentUsage  = currentUsage *1000 / FREQUENCY_MS; //get usage per seconds converted to frequency

		double iPart = (long) currentUsage; //get int part
		double fPart = currentUsage - iPart; //get fractional part
		log.v("Current usage for frequency "+FREQUENCY_MS+"ms/cycle is "+currentUsage+".");

		if(random.nextDouble() > fPart) { //make fractional part to int by rounding by chance
			iPart++;
		}

		log.v("Adding "+iPart+" tasks.");

		for(int i=0;i<iPart;i++) {
			createTask(); //create task according to usage
		}
	}

	/**
	 * Creates a task and all subtasks according to its template
	 */
	private void createTask() {
		ITask task = new Task(template);

		for (ISubTaskTemplate t : template.getSubTaskList()) {
			ISubTask subTask = null;
			if (t.getTaskType() == ISubTask.TaskType.PROCESSING) {
				log.v("Create task for Processor");
				subTask = new ComputationalSubTask((ComputationalSubTaskTemplate)t);
			} else if (t.getTaskType() == ISubTask.TaskType.NETWORK_IO) {
				log.v("Create task for Network");
				//TODO
			} else if (t.getTaskType() == ISubTask.TaskType.DISK_IO) {
				log.v("Create task for Disk");
				//TODO
			}
			task.addSubTask(subTask);
		}

		Env.get().getZone(zoneIds.peek()).addTask(task);
	}

	@Override
	public String toString() {
		return "[PRODUCER]";
	}
}
