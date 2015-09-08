package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.PropertyInterface;


public class StringFormElement extends  AbstractFormElement<String> implements Enable {

	private final int maxLength;
	private final Input<String> textField;

	public StringFormElement(PropertyInterface property) {
		this(property, null);
	}
	
	public StringFormElement(PropertyInterface property, Boolean multiLine) {
		super(property);
		this.maxLength = AnnotationUtil.getSize(property);
		if (maxLength < 256 && !multiLine) {
			this.textField = Frontend.getInstance().createTextField(maxLength, null, InputType.FREE, null, listener());
		} else {
			this.textField = Frontend.getInstance().createAreaField(maxLength, null, listener());
		}
	}

	@Override
	public IComponent getComponent() {
		return textField;
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
