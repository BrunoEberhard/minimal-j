package org.minimalj.frontend.form.element;

import java.util.Arrays;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.PasswordField;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.AnnotationUtil;

public class PasswordFormElement extends AbstractFormElement<char[]> {

	private final int maxLength;
	private final PasswordField textField;

	public PasswordFormElement(char[] key) {
		super(Keys.getProperty(key));
		this.maxLength = AnnotationUtil.getSize(Keys.getProperty(key));
		this.textField = Frontend.getInstance().createPasswordField(listener(), maxLength);
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public void setValue(char[] value) {
		if (value != null && value.length > maxLength) {
			value = Arrays.copyOfRange(value, 0, maxLength);
		}
		textField.setValue(value);
	}

	@Override
	public char[] getValue() {
		return textField.getValue();
	}
	
}
