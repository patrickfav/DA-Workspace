package at.ac.tuwien.e0426099.test.processor.single;

import at.ac.tuwien.e0426099.simulator.environment.G;
import at.ac.tuwien.e0426099.simulator.environment.zone.ZoneId;
import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ITask;
import at.ac.tuwien.e0426099.test.processor.AProcessorTest;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author PatrickF
 * @since 13.12.12
 */
public class ConfigAbleTest1 extends AProcessorTest{
	private Logger log = LogManager.getLogger(this.getClass().getName());

	@Before
	public void setUp() {
		super.setUp();
		taskList = generateFixedTasks(4,4);
	}

	@Test(timeout = 180000)
	public void testSubTaskStartShouldFinish() {
		ZoneId id = G.get().addPlatform("local",defaultDualCoreUnit);

		for(ITask t:taskList) {
			G.get().getPlatform(id).addTask(t);
		}

		G.get().start();
		
		assertTrue(G.get().waitForFinish());
	}
}
