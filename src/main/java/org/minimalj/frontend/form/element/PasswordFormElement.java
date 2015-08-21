package org.minimalj.frontend.form.element;

import java.util.Arrays;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.PasswordField;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.AnnotationUtil;

public class PasswordFormElement extends AbstractFormElement<Object> {

	private final int maxLength;
	private final PasswordField textField;

	public PasswordFormElement(Object key) {
		super(Keys.getProperty(key));
		this.maxLength = AnnotationUtil.getSize(Keys.getProperty(key));
		this.textField = Frontend.getInstance().createPasswordField(listener(), maxLength);
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof String) {
			String string = (String) value;
			if (maxLength > 0 && string.length() > maxLength) {
				string = string.substring(0, maxLength);
			}
			textField.setValue(string);
		} else if (value instanceof char[]) {
			char[] chars = (char[]) value;
			if (chars.length > maxLength) {
				chars = Arrays.copyOfRange(chars, 0, maxLength);
			}
			textField.setValue(chars);
		}
	}

	@Override
	public Object getValue() {
		return textField.getValue();
	}
	
}
