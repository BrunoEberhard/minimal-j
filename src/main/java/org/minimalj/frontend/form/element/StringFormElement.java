package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.Autocomplete;
import org.minimalj.model.annotation.Autocomplete.Autocompletable;
import org.minimalj.model.properties.Property;


public class StringFormElement extends  AbstractFormElement<String> implements Enable {

	public static final int MULTI_LINE = 3;
	public static final int SINGLE_LINE = 1;

	private final int lines;
	private final int maxLength;
	private final Input<String> textField;

	public StringFormElement(String key, int lines) {
		this(Keys.getProperty(key), lines);
	}

	public StringFormElement(Property property) {
		this(property, AnnotationUtil.getSize(property) < 256 ? SINGLE_LINE : MULTI_LINE);
	}
	
	public StringFormElement(Property property, int lines) {
		super(property);
		this.lines = lines;
		this.maxLength = AnnotationUtil.getSize(property);
		if (lines == 1) {
			this.textField = Frontend.getInstance().createTextField(maxLength, null, null, listener());
		} else {
			this.textField = Frontend.getInstance().createAreaField(maxLength, null, listener());
		}
		if (this.textField instanceof Autocompletable) {
			Autocomplete autocomplete = property.getAnnotation(Autocomplete.class);
			if (autocomplete != null) {
				((Autocompletable) textField).setAutocomplete(autocomplete.value());
			}
		}
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public FormElementConstraint getConstraint() {
		if (lines != 1) {
			return new FormElementConstraint(1, lines);
		} else {
			return null;
		}
	}

	@Override
	public void setValue(String string) {
		if (string != null) {
			if (maxLength > 0 && string.length() > maxLength) {
				string = string.substring(0, maxLength);
			}
		}
		textField.setValue(string);
	}

	@Override
	public String getValue() {
		return textField.getValue();
	}

	@Override
	public void setEnabled(boolean enabled) {
		textField.setEditable(enabled);
	}
	
}
