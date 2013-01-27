package at.ac.tuwien.e0426099.simulator.environment.zone.processor.entities;

import at.ac.tuwien.e0426099.simulator.environment.task.entities.SubTaskId;
import at.ac.tuwien.e0426099.simulator.environment.zone.processor.ProcessingCore;

/**
 * Wraps an action and the entity to do it to
 *
 * @author PatrickF
 * @since 23.01.13
 */
public class ActionWrapper {
	public enum ActionType {ADD,FINISH,FAIL}
	private SubTaskId subTaskId;
	private ProcessingCore core;
	private ActionType actionType;

	public ActionWrapper(SubTaskId subTask, ActionType actionType) {
		this.subTaskId = subTask;
		this.actionType = actionType;
	}

	public ActionWrapper(SubTaskId subTaskId, ActionType actionType,  ProcessingCore core) {
		this.subTaskId = subTaskId;
		this.core = core;
		this.actionType = actionType;
	}

	public ProcessingCore getCore() {
		return core;
	}

	public SubTaskId getSubTaskId() {
		return subTaskId;
	}

	public ActionType getActionType() {
		return actionType;
	}
}
