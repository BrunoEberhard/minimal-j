package org.minimalj.frontend.action;

import java.text.MessageFormat;
import java.util.Objects;

import org.minimalj.frontend.form.Form;
import org.minimalj.model.Rendering;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Action is used for at several points. But not everywhere all of its
 * behavior is supported. See the javadoc of the setter for more information.
 *
 */
public abstract class Action implements Runnable, Rendering {

	private String name, description;
	private Boolean descriptionAvailable;
	private boolean enabled = true;
	private ActionChangeListener changeListener;

	/**
	 * The name and description of this action will be defined by the
	 * class name.
	 */
	protected Action() {
	}
	
	protected String getResourceName() {
		return Resources.getResourceName(getClass());
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
		if (name == null) {
			String resourceName = getResourceName();
			Object[] nameArguments = getNameArguments();
			name = nameArguments != null ? MessageFormat.format(Resources.getString(resourceName), nameArguments) : Resources.getString(resourceName);
		}
		return name;
	}
	
	/**
	 * 
	 * @return this could be used by a frontend for a additional information about this action. For example a tooltip.
	 */
	public String getDescription() {
		if (description == null && !Boolean.FALSE.equals(descriptionAvailable)) {
			String resourceName = getResourceName();
			String descriptionResourceName = resourceName + ".description";
			descriptionAvailable = Resources.isAvailable(descriptionResourceName);
			if (descriptionAvailable) {
				this.description = Resources.getString(descriptionResourceName);
			}
		}
		return description;
	}
	
	@Override
	public CharSequence render() {
		return getName();
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

	public void setChangeListener(ActionChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	public interface ActionChangeListener {
		public void change();
	}
	
	public abstract static class ValidationAwareAction extends Action {
		private Form<?> form;
		
		public void setForm(Form<?> form) {
			this.form = Objects.requireNonNull(form);
			fireChange();
		}
		
		public Form<?> getForm() {
			if (this.form == null) {
				throw new IllegalStateException("Form of " + ValidationAwareAction.class.getSimpleName() +" not set");
			}
			return form;
		}
	}
}
