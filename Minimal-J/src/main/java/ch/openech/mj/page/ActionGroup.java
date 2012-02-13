package ch.openech.mj.page;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;

public class ActionGroup extends AbstractAction {

	public static final String NEW = "new";
	
	private final List<Action> actions = new ArrayList<Action>();

	public ActionGroup() {
	}
	
	public ActionGroup(String name) {
		super(name);
	}
	
	public void add(Action action) {
		actions.add(action);
	}
	
	public List<Action> getActions() {
		return actions;
	}

	/**
	 * 
	 * @return Recursivly collected actions but not the ActionGroups
	 */
	public List<Action> getAllActions() {
		List<Action> allActions = new ArrayList<Action>();
		for (Action action : getActions()) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (ActionGroup) action;
				allActions.addAll(actionGroup.getAllActions());
			} else {
				allActions.add(action);
			}
		}
		return allActions;
	}
		
	/**
	 * 
	 * @param resourceName Name of resource. A prefix "ActionGroup." is added.
	 * @return existing or newly created ActionGroup
	 */
	public ActionGroup getOrCreateActionGroup(String resourceName) {
		ActionGroup actionGroup = getActionGroup(resourceName);
		if (actionGroup != null) {
			return actionGroup;
		} else {
			return createActionGroup(resourceName);
		}
	}

	/**
	 * 
	 * @param resourceName Name of resource. A prefix "ActionGroup." is added.
	 * @return new ActionGroup added to this one
	 */
	public ActionGroup createActionGroup(String resourceName) {
		ActionGroup actionGroup = new ActionGroup(resourceName);
		ResourceHelper.initProperties(actionGroup, Resources.getResourceBundle(), "ActionGroup." + resourceName);
		add(actionGroup);
		return actionGroup;
	}
	
	private ActionGroup getActionGroup(String name) {
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (ActionGroup) action;
				if (name.equals(actionGroup.getValue(NAME))) {
					return actionGroup;
				}
			}
		}
		return null;
	}
	
	public void addSeparator() {
		if (!actions.isEmpty() && !(actions.get(actions.size() - 1) instanceof SeparatorAction)) {
			actions.add(new SeparatorAction());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// unused
	}

}
