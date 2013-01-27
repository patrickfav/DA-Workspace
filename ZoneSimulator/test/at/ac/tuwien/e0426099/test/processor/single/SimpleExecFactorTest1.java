package at.ac.tuwien.e0426099.test.processor.single;

import at.ac.tuwien.e0426099.simulator.environment.Env;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.test.processor.AProcessorTest;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author PatrickF
 * @since 13.12.12
 */
public class SimpleExecFactorTest1 extends AProcessorTest{

	@Before
	public void setUp() {
		super.setUp();
		taskList = generateFixedTasks(4,4);
	}

	@Test(timeout = 180000)
	public void testSubTaskStartShouldFinish() {
		ZoneId id = Env.get().addZone("local", defaultDualCoreUnit);

		for(ITask t:taskList) {
			Env.get().getZone(id).addTask(t);
		}

		Env.get().start(1.1);

		sleepUninterruptly(2*1000);
		Env.get().pause();
		sleepUninterruptly(5*1000);

		Env.get().resume(2.0);

		sleepUninterruptly(2*1000);
		Env.get().pause();
		sleepUninterruptly(5*1000);

		Env.get().resume(0.75);

		sleepUninterruptly(2*1000);
		Env.get().pause();
		sleepUninterruptly(5*1000);

		Env.get().resume(0.5);

		sleepUninterruptly(2*1000);
		Env.get().pause();
		sleepUninterruptly(5*1000);

		Env.get().resume(0.1);

		sleepUninterruptly(2*1000);
		Env.get().pause();
		sleepUninterruptly(5*1000);

		Env.get().resume(1.0);

		assertTrue(Env.get().waitForFinish());
	}
}
