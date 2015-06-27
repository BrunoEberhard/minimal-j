package org.minimalj.frontend.toolkit;

import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Action is used for at several points. But not everywhere all of its
 * behavior is supported. See the javadoc of the setter for more information.
 *
 */
public abstract class Action {

	private String name;
	private String description;
	private boolean enabled = true;
	private ActionChangeListener changeListener;

	/**
	 * The name and description of this action will be defined by the
	 * class name.
	 */
	protected Action() {
		this(null);
	}
	
	protected Action(String actionName) {
		this.name = actionName == null ? Resources.getActionName(getClass()) : Resources.getString(actionName);
		if (actionName != null) {
			this.description = Resources.getString(actionName + ".description", Resources.OPTIONAL);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Note: this is not supported if the action was used to create
	 * a label by the ClientToolkit
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			fireChange();
		}
	}

	/**
	 * Note: this is not supported yet. You cannot change name
	 * of an action.
	 * 
	 * @param name New name display for this action
	 */
	public void setName(String name) {
		if (!StringUtils.equals(this.name, name)) {
			throw new IllegalArgumentException("Name of action cannot be changed");
		}
	}

	/**
	 * Note: this is only supported for actions used in an Editor
	 * 
	 * @param description New description (tooltip) of this action
	 */
	public void setDescription(String description) {
		if (!StringUtils.equals(this.description, description)) {
			this.description = description;
			fireChange();
		}
	}

	protected void fireChange() {
		if (changeListener != null) {
			changeListener.change();
		}
	}

	public abstract void action();

	public void setChangeListener(ActionChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	public interface ActionChangeListener {
		public void change();
	}
}
