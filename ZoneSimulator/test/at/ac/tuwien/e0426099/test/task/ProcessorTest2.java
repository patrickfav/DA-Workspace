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

/**
 * @author PatrickF
 * @since 13.12.12
 */
public class ProcessorTest2 {
	private Logger log = LogManager.getLogger(this.getClass().getName());

	private ComputationalSubTask subTask1,subTask2,subTask3,subTask4,subTask5;
	private ComputationalSubTask subTask6,subTask7,subTask8,subTask9,subTask10;
	private List<ComputationalSubTask> taskList = new ArrayList<ComputationalSubTask>();

	@Before
	public void setUp() {
		subTask1 = new ComputationalSubTask("TestSubTask1",new MemoryAmount(10),10l,30000l);
		subTask2 = new ComputationalSubTask("TestSubTask2",new MemoryAmount(15),15l,40000l);
		subTask3 = new ComputationalSubTask("TestSubTask3",new MemoryAmount(15),10l,10000l);
		subTask4 = new ComputationalSubTask("TestSubTask4",new MemoryAmount(15),50l,1000l);
		subTask5 = new ComputationalSubTask("TestSubTask5",new MemoryAmount(15),5l,20000l);
		subTask6 = new ComputationalSubTask("TestSubTask6",new MemoryAmount(10),10l,60000l);
		subTask7 = new ComputationalSubTask("TestSubTask7",new MemoryAmount(15),15l,30000l);
		subTask8 = new ComputationalSubTask("TestSubTask8",new MemoryAmount(15),10l,15000l);
		subTask9 = new ComputationalSubTask("TestSubTask9",new MemoryAmount(15),30l,20000l);
		subTask10 = new ComputationalSubTask("TestSubTask10",new MemoryAmount(15),5l,30000l);

	}

	@After
	public void tearDown() {

	}


	@Test
	public void testSubTaskStartShouldFinish() {
		WorkingMemory memory = new WorkingMemory(new MemoryAmount(4 * 100 * 1000),0.5);
		ProcessingCore core1 = new ProcessingCore(new RawProcessingPower(1000),5,0.05);
		ProcessingCore core2 = new ProcessingCore(new RawProcessingPower(1500),5,0.07);

		List<ProcessingCore> cores = new ArrayList<ProcessingCore>();
		cores.add(core1);
		cores.add(core2);

		ProcessingUnit unit = new ProcessingUnit(new FifoLeastLoadScheduler(),memory,cores);

		PlatformId id = G.get().addPlatform("local",unit);

		ITask task1 = new Task("No1");

		task1.addSubTask(subTask1);
		task1.addSubTask(subTask3);
		task1.addSubTask(subTask4);
		task1.addSubTask(subTask6);
		task1.addSubTask(subTask8);

		ITask task2 = new Task("No2");
		task2.addSubTask(subTask2);
		task2.addSubTask(subTask5);
		task2.addSubTask(subTask7);
		task2.addSubTask(subTask9);
		task2.addSubTask(subTask10);

		G.get().getPlatform(id).addTask(task1);
		G.get().getPlatform(id).addTask(task2);

		G.get().start();

        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        G.get().pause();


        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        G.get().resume();

		G.get().waitForFinish();


	}
}
