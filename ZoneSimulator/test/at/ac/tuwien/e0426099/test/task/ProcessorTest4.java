package at.ac.tuwien.e0426099.test.task;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.platform.PlatformId;
import at.ac.tuwien.e0426099.simulator.environment.platform.memory.WorkingMemory;
import at.ac.tuwien.e0426099.simulator.environment.platform.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.ProcessingCore;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.ProcessingUnit;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.platform.processor.scheduler.FifoLeastLoadScheduler;
import at.ac.tuwien.e0426099.simulator.environment.task.ComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.Task;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertTrue;

/**
 * @author PatrickF
 * @since 13.12.12
 */
public class ProcessorTest4 {
	private Logger log = LogManager.getLogger(this.getClass().getName());

	Random r = new Random(123456789);

	private List<ITask> taskList = new ArrayList<ITask>();
	@Before
	public void setUp() {
		for(int i=0;i<3;i++) {
			taskList.add(i,new Task("No"+i));
			for(int j=0;j<7;j++) {
				taskList.get(i).addSubTask(new ComputationalSubTask("SubT"+i+"-"+j,new MemoryAmount(10),getLongInRange(5,30),getLongInRange(3000,80000)));
			}
		}
	}

	private long getLongInRange(long min, long max) {
		return min + (int)(r.nextDouble() * ((max - min) + 1));
	}

	@After
	public void tearDown() {

	}


	@Test
	public void testSubTaskStartShouldFinish() {
		WorkingMemory memory = new WorkingMemory(new MemoryAmount(4 * 100 * 1000),0.5);
		ProcessingCore core1 = new ProcessingCore(new RawProcessingPower(1000),10,0.05);
		ProcessingCore core2 = new ProcessingCore(new RawProcessingPower(1500),10,0.07);

		List<ProcessingCore> cores = new ArrayList<ProcessingCore>();
		cores.add(core1);
		cores.add(core2);

		ProcessingUnit unit = new ProcessingUnit(new FifoLeastLoadScheduler(),memory,cores);

		PlatformId id = G.get().addPlatform("local",unit);

		for(ITask t:taskList) {
			G.get().getPlatform(id).addTask(t);
		}

		G.get().start();
		
		assertTrue(G.get().waitForFinish());


	}
}
