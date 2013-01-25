package at.ac.tuwien.e0426099.test.processor.batch;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import at.ac.tuwien.e0426099.test.processor.AProcessorTest;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author PatrickF
 * @since 23.01.13
 */
public class IntensivatingTests extends AProcessorTest{

	@Test(timeout = 60000)
	public void testSubTaskStartShouldFinish2x1() {
		testSubTaskStartShouldFinish(2,1);
	}

	@Test(timeout = 60000)
	public void testSubTaskStartShouldFinish2x2() {
		testSubTaskStartShouldFinish(2,2);
	}

	@Test(timeout = 60000)
	public void testSubTaskStartShouldFinish3x2() {
		testSubTaskStartShouldFinish(3,2);
	}
	@Test(timeout = 60000)
	public void testSubTaskStartShouldFinish3x3() {
		testSubTaskStartShouldFinish(3,4);
	}

	@Test(timeout = 60000)
	public void testSubTaskStartShouldFinish4x3() {
		testSubTaskStartShouldFinish(4,3);
	}

	@Test(timeout = 60000)
	public void testSubTaskStartShouldFinish4x4() {
		testSubTaskStartShouldFinish(4,4);
	}
	@Test(timeout = 60000)
	public void testSubTaskStartShouldFinish3x5() {
		testSubTaskStartShouldFinish(3,5);
	}

	@Test(timeout = 60000)
	public void testSubTaskStartShouldFinish3x8() {
		testSubTaskStartShouldFinish(3,8);
	}

	@Test(timeout = 180000)
	public void testSubTaskStartShouldFinish20x2() {
		testSubTaskStartShouldFinish(40,3);
	}



	private void testSubTaskStartShouldFinish(int tasks, int subtasksPerTask) {
		ZoneId id = G.get().addZone("local", defaultDualCoreUnit);

		for(ITask t:generateFixedTasks(tasks,subtasksPerTask)) {
			G.get().getZone(id).addTask(t);
		}

		G.get().start();
		assertTrue(G.get().waitForFinish());
	}
}
