package at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities;

import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;

/**
 * Wraps an action and the entity to do it to
 *
 * @author PatrickF
 * @since 23.01.13
 */
public class ActionWrapper {
	public enum ActionType {ADD,REMOVE}
	private SubTaskId subTaskId;
	private ActionType actionType;

	public ActionWrapper(SubTaskId subTask, ActionType actionType) {
		this.subTaskId = subTask;
		this.actionType = actionType;
	}

	public SubTaskId getSubTaskId() {
		return subTaskId;
	}

	public ActionType getActionType() {
		return actionType;
	}
}
