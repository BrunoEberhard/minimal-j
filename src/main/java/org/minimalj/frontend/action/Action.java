package org.minimalj.frontend.action;

import java.text.MessageFormat;

import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Action is used for at several points. But not everywhere all of its
 * behavior is supported. See the javadoc of the setter for more information.
 *
 */
public abstract class Action {

	private final String name;
	private String description;
	private boolean enabled = true;
	private ActionChangeListener changeListener;

	/**
	 * The name and description of this action will be defined by the
	 * class name.
	 */
	protected Action() {
		String resourceName = Resources.getResourceName(getClass());
		Object[] nameArguments = getNameArguments();
		this.name = nameArguments != null ?  MessageFormat.format(Resources.getString(resourceName), nameArguments) : Resources.getString(resourceName);
		
		String descriptionResourceName = resourceName + ".description";
		if (Resources.isAvailable(descriptionResourceName)) {
			this.description = Resources.getString(descriptionResourceName);
		}
	}
	
	protected Object[] getNameArguments() {
		return null;
	}
	
	protected Action(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @return the text normally displayed in the frontend
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return this could be used by a frontend for a additional information about this action. For example a tooltip.
	 */
	public String getDescription() {
		return description;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * @param enabled Note: this is not supported if the action was used to create
	 * a label by the Frontend
	 */
	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			fireChange();
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
