package org.minimalj.frontend.form.element;

import java.util.List;
import java.util.Optional;

import javax.swing.SwingConstants;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.impl.json.JsonComponent;
import org.minimalj.frontend.impl.json.JsonTextField;
import org.minimalj.frontend.impl.swing.toolkit.SwingTextField;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.StringUtils;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

public abstract class FormatFormElement<T> extends AbstractFormElement<T> implements Enable, Mocking {

	private final boolean editable;
	
	/*
	 * textField is instantiated lazy because callbacks are used
	 * and the subclasses need a chance to initialize the values
	 */
	protected Input<String> textField;
	
	/*
	 * typed: Frontends may or may not provide special Inputs for date or time. Even
	 * the same Frontend can sometimes support those Inputs and sometimes not. For
	 * example a HTML Frontend depends on the used Browser. FireFox and Microsoft didn't
	 * support date and time Inputs for a long time.
	 * If typed is true then the FormElement must format the date or time without
	 * considering the current Locale. If typed is false a normal text Input is
	 * used for the date or time. Then the FormElement must to the formatting according
	 * to the current Locale.
	 */
	protected boolean typed;
	
	/*
	 * only used if setValue is called before getComponent()
	 */
	private String setText;
	
	public FormatFormElement(Property property, boolean editable) {
		super(property);
		this.editable = editable;
	}

	protected abstract String getAllowedCharacters(Property property);

	protected abstract int getAllowedSize(Property property);

	protected InputType getInputType() {
		return InputType.TEXT;
	}

	protected Search<String> getSearch(Property property) {
		return null;
	}

	@Override
	public IComponent getComponent() {
		if (textField == null) {
			if (editable) {
				Optional<Input<String>> typeTextField = Frontend.getInstance().createInput(getAllowedSize(getProperty()), getInputType(), new TextFormatFieldChangeListener());
				typed = typeTextField.isPresent();
				if (typed) {
					textField = typeTextField.get();
				} else {
					textField = Frontend.getInstance().createTextField(getAllowedSize(getProperty()), getAllowedCharacters(getProperty()),
							getSearch(getProperty()), new TextFormatFieldChangeListener());
				}
				
			} else {
				textField = Frontend.getInstance().createReadOnlyTextField();
			}
			// TODO find general concept for placeholer
			if (textField instanceof JsonTextField) {
				String placeholder = Resources.getPropertyName(getProperty(), "placeholder");
				if (placeholder != null) {
					((JsonTextField) textField).setPlaceholder(placeholder);
				}
				if (Number.class.isAssignableFrom(getProperty().getClazz())) {
					((JsonComponent) textField).setCssClass("textAlignRight");
				}
			} else if (textField instanceof SwingTextField) {
				String placeholder = Resources.getPropertyName(getProperty(), "placeholder");
				if (placeholder != null) {
					((SwingTextField) textField).setPlaceholderText(placeholder);
				}
				if (Number.class.isAssignableFrom(getProperty().getClazz())) {
					((SwingTextField) textField).setHorizontalAlignment(SwingConstants.RIGHT);
				}
			}
			textField.setValue(setText);
		}
		return textField;
	}

	@Override
	public final T getValue() {
		return parse(textField.getValue());
	}

	/**
	 * @param text input by the user
	 * @return the parsed value. Or if text cannot be parsed a invalid value.
	 * @see InvalidValues
	 */
	protected abstract T parse(String text);
	
	@Override
	public final void setValue(T value) {
		setText = InvalidValues.isInvalid(value) ? InvalidValues.getInvalidValue(value) : render(value);
		if (textField != null && !StringUtils.equals(setText, textField.getValue())) {
			textField.setValue(setText);
		}
	}

	/**
	 * @param value valid or null but not invalid (already checked in setValue() )
	 * @return the formatted value
	 * @see #setValue(Object)
	 */
	protected abstract String render(T value);
	
	private class TextFormatFieldChangeListener implements InputComponentListener {
		@Override
		public void changed(IComponent source) {
			// Redo format
			T value = getValue();
			boolean valid = !InvalidValues.isInvalid(value);
			if (value instanceof Validation) {
				List<ValidationMessage> validationMessages = ((Validation) value).validateNullSafe();
				valid &= validationMessages.isEmpty();
			}

			if (valid) {
				setValue(value);
			}
			
			fireChange();
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		getComponent(); // ensure textField is initialized
		textField.setEditable(enabled);
	}

	@Override
	public boolean canBeEmpty() {
		// if not editable it cannot be users fault that it is empty (don't show 'required' mark)
		return editable;
	}
}
