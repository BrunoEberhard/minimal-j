package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.model.validation.Validatable;
import org.minimalj.util.mock.Mocking;

public abstract class FormatFormElement<T> extends AbstractFormElement<T> implements Enable, Mocking {

	private final boolean editable;
	
	/*
	 * textField is instantiated lazy because callbacks are used
	 * and the subclasses need a chance to initialize the values
	 */
	protected Input<String> textField;

	public FormatFormElement(PropertyInterface property, boolean editable) {
		super(property);
		this.editable = editable;
	}

	protected abstract String getAllowedCharacters(PropertyInterface property);

	protected abstract int getAllowedSize(PropertyInterface property);

	protected InputType getInputType() {
		return InputType.FREE;
	}

	@Override
	public IComponent getComponent() {
		if (textField == null) {
			if (editable) {
				textField = Frontend.getInstance().createTextField(getAllowedSize(getProperty()), getAllowedCharacters(getProperty()),
						getInputType(), null, new TextFormatFieldChangeListener());
			} else {
				textField = Frontend.getInstance().createReadOnlyTextField();
			}

		}
		return textField;
	}

	public abstract T getValue();

	public abstract void setValue(T value);

	private class TextFormatFieldChangeListener implements InputComponentListener {
		@Override
		public void changed(IComponent source) {
			// Formattierung auslösen
			T value = getValue();
			boolean valid = true;
			valid &= !InvalidValues.isInvalid(value);
			valid &= !(value instanceof Validatable) || ((Validatable) value).validate() == null;

			if (valid) {
				setValue(value);
			}
			
			fireChange();
		}
	}
	
	public void setEnabled(boolean enabled) {
		textField.setEditable(enabled);
	}

}
