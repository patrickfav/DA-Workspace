package at.ac.tuwien.e0426099.simulator.environment.task.producer.templates;

import at.ac.tuwien.e0426099.simulator.environment.task.interfaces.ISubTaskTemplate;

import java.util.List;

/**
 * @author PatrickF
 * @since 25.01.13
 */
public class TaskTemplate {
	private String readAblenName;
	private List<ISubTaskTemplate> subTaskList;

	public TaskTemplate(String readAblenName) {
		this.readAblenName = readAblenName;
	}

	public String getReadAblenName() {
		return readAblenName;
	}

	public List<ISubTaskTemplate> getSubTaskList() {
		return subTaskList;
	}
}
