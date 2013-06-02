package ch.openech.mj.page;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;

/**
 * Not really an action, but an aggregation of other actions. Used to build
 * menu item groups.
 * 
 */
public class ActionGroup extends AbstractAction {

	private static final String TAG = "tag";

	// Values for putValue on Action
	public static final String FILE = "file";
	public static final String NEW = "new";
	public static final String IMPORT = "import";
	public static final String EXPORT = "export";

	public static final String VIEW = "view";
	public static final String EDIT = "edit";
	public static final String OBJECT = "object";
	public static final String WINDOW = "window";
	public static final String HELP = "help";
	
	private final List<Action> actions = new ArrayList<Action>();

	public ActionGroup() {
		super();
	}

	public ActionGroup(String tag) {
		super();
		putValue(TAG, tag);
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), "ActionGroup." + tag);
	}
	
	public void add(Action action) {
		actions.add(action);
	}
	
	public String getTag() {
		return (String) getValue(TAG);
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
	 * @param tag tag of the group. Also used for Resources, a prefix "ActionGroup." is added
	 * @return existing or newly created ActionGroup
	 */
	public ActionGroup getOrCreateActionGroup(String tag) {
		ActionGroup actionGroup = getActionGroup(tag);
		if (actionGroup != null) {
			return actionGroup;
		} else {
			return createActionGroup(tag);
		}
	}

	/**
	 * 
	 * @param tag tag of the group. Also used for Resources, a prefix "ActionGroup." is added
	 * @return new ActionGroup added to this one
	 */
	public ActionGroup createActionGroup(String tag) {
		ActionGroup actionGroup = new ActionGroup(tag);
		add(actionGroup);
		return actionGroup;
	}
	
	private ActionGroup getActionGroup(String name) {
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (ActionGroup) action;
				if (name.equals(actionGroup.getTag())) {
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
