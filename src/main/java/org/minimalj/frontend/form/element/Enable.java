package org.minimalj.frontend.form.element;

import org.minimalj.model.annotation.Enabled;

public interface Enable {

	/**
	 * This should not be called from application code directly. Instead the field in the model class
	 * belonging to the form field should be annotated with {@link Enabled}.
	 * 
	 * @param enabled the new status of the FormElement
	 */
	public void setEnabled(boolean enabled);
	
}
