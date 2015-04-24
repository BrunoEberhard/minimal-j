package org.minimalj.frontend.form.element;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.InputType;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.PropertyInterface;


public class StringFormElement extends  AbstractFormElement<String> implements Enable {

	private final int maxLength;
	private final TextField textField;

	public StringFormElement(PropertyInterface property) {
		this(property, null);
	}
	
	public StringFormElement(PropertyInterface property, Boolean multiLine) {
		super(property);
		this.maxLength = AnnotationUtil.getSize(property);
		this.textField = ClientToolkit.getToolkit().createTextField(maxLength, null, InputType.FREE, null, listener());
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public void setObject(String string) {
		if (string != null) {
			if (maxLength > 0 && string.length() > maxLength) {
				string = string.substring(0, maxLength);
			}
		}
		textField.setValue(string);
	}

	@Override
	public String getObject() {
		return textField.getValue();
	}

	@Override
	public void setEnabled(boolean enabled) {
		textField.setEditable(enabled);
	}
	
}
