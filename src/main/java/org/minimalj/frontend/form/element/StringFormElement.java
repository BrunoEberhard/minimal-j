package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.PropertyInterface;


public class StringFormElement extends  AbstractFormElement<String> implements Enable {

	public static final int MULTI_LINE = 3;
	public static final int SINGLE_LINE = 1;

	private final int lines;
	private final int maxLength;
	private final Input<String> textField;

	public StringFormElement(String key, int lines) {
		this(Keys.getProperty(key), lines);
	}

	public StringFormElement(PropertyInterface property) {
		this(property, AnnotationUtil.getSize(property) < 256 ? SINGLE_LINE : MULTI_LINE);
	}
	
	public StringFormElement(PropertyInterface property, int lines) {
		super(property);
		this.lines = lines;
		this.maxLength = AnnotationUtil.getSize(property);
		if (lines == 1) {
			this.textField = Frontend.getInstance().createTextField(maxLength, null, null, listener());
		} else {
			this.textField = Frontend.getInstance().createAreaField(maxLength, null, listener());
		}
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public FormElementConstraint getConstraint() {
		if (lines != 1) {
			return new FormElementConstraint(lines, lines, false);
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
