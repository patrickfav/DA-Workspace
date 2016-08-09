package at.ac.tuwien.e0426099.test.processor;

import at.ac.tuwien.e0426099.simulator.environment.Env;
import at.ac.tuwien.e0426099.simulator.environment.zone.memory.WorkingMemory;
import at.ac.tuwien.e0426099.simulator.environment.zone.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.ProcessingCore;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.ProcessingUnit;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.scheduler.FifoLeastLoadScheduler;
import at.ac.tuwien.e0426099.simulator.environment.task.ComputationalSubTask;
import at.ac.tuwien.e0426099.simulator.environment.task.Task;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import at.ac.tuwien.e0426099.test.AZoneSimTest;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PatrickF
 * @since 23.01.13
 */
public abstract class AProcessorTest extends AZoneSimTest {
	protected ComputationalSubTask subTask1,subTask2,subTask3,subTask4,subTask5;
	protected ComputationalSubTask subTask6,subTask7,subTask8,subTask9,subTask10;

	protected ProcessingUnit defaultDualCoreUnit;

	protected List<ITask> taskList = new ArrayList<ITask>();

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


		WorkingMemory memory = new WorkingMemory(new MemoryAmount(4 * 100 * 1000),0.5);
		ProcessingCore core1 = new ProcessingCore(new RawProcessingPower(1500),10,0.03);
		ProcessingCore core2 = new ProcessingCore(new RawProcessingPower(2000),10,0.02);

		List<ProcessingCore> cores = new ArrayList<ProcessingCore>();
		cores.add(core1);
		cores.add(core2);

		defaultDualCoreUnit = new ProcessingUnit(new FifoLeastLoadScheduler(),memory,cores);
	}

	@After
	public void tearDown() {
		Env.recycle();
	}


	protected List<ITask> generateFixedTasks(int tasks, int subtasksPerTask) {
		List<ITask> taskList = new ArrayList<ITask>();
		for(int i=0;i<tasks;i++) {
			taskList.add(i,new Task("No"+i));
			for(int j=0;j<subtasksPerTask;j++) {
				taskList.get(i).addSubTask(new ComputationalSubTask("SubT"+i+"-"+j,new MemoryAmount(10),getFixedLongInRange(5, 30),getFixedLongInRange(3000, 80000)));
			}
		}
		return taskList;
	}
}
