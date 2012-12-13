package at.ac.tuwien.e0426099.test.task;

import at.ac.tuwien.e0426099.simulator.environment.memory.entities.MemoryAmount;
import at.ac.tuwien.e0426099.simulator.environment.processor.entities.RawProcessingPower;
import at.ac.tuwien.e0426099.simulator.environment.task.ComputationalSubTask;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;

/**
 * @author PatrickF
 * @since 09.12.12
 */
public class TestSubTask {
	private Logger log = LogManager.getLogger(TestSubTask.class.getName());

	private ComputationalSubTask subTask1,subTask2,subTask3;
	private List<ComputationalSubTask> taskList = new ArrayList<ComputationalSubTask>();
	@Before
	public void setUp() {
		subTask1 = new ComputationalSubTask("TestSubTask1",new MemoryAmount(10),10l,30000l);
		subTask2 = new ComputationalSubTask("TestSubTask2",new MemoryAmount(15),15l,40000l);
		subTask3 = new ComputationalSubTask("TestSubTask3",new MemoryAmount(15),10l,5000l);

		subTask1.updateAvailableProcessingPower(new RawProcessingPower(10l));
		subTask2.updateAvailableProcessingPower(new RawProcessingPower(20l));
		subTask3.updateAvailableProcessingPower(new RawProcessingPower(10l));

		taskList.add(subTask1);
		taskList.add(subTask2);
		taskList.add(subTask3);
	}

	@After
	public void tearDown() {
		//wait for threads to finish
		for(ComputationalSubTask t:taskList) {
			t.waitForTaskToFinish();
		}
		log.info("========================================================");
	}


	@Test
	public void testSubTaskStartShouldFinish() {
		subTask1.run();
		subTask2.run();
		subTask3.run();
	}

	@Test
	public void testSubTaskStartPauseRestartShouldFinish() {
		subTask2.run();

		waitInTest(500);

		subTask2.pause();

		waitInTest(500);

		subTask2.run();
	}

	@Test
	public void testSubTaskStartAndSimulatedFail() {
		subTask2.run();

		waitInTest(200);

		subTask2.pause();

		waitInTest(100);

		subTask2.run();

		waitInTest(100);

		subTask2.fail(new NullPointerException());

		waitInTest(100);
	}

	private void waitInTest(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}
}
