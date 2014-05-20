package org.minimalj.frontend.edit.form;

import java.util.List;

import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.model.validation.ValidationMessage;

public interface IForm<T> {

	public IComponent getComponent();
	
	public void setObject(T value);

	/**
	 * Set the ChangeListener<br>
	 * note: The default implementation form needs that the listener
	 * is set before the first call of setObject. It cannot be changed
	 * afterwards.
	 * 
	 * @param listener not <code>null</code>
	 */
	public void setChangeListener(FormChangeListener<T> listener);

	public interface FormChangeListener<S> {

		public void validate(S object, List<ValidationMessage> validationResult);

		public void indicate(List<ValidationMessage> validationMessages, boolean allUsedFieldsValid);

		public void changed();

		public void commit();

	}
}