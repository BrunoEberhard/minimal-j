package org.minimalj.frontend.form.element;

import java.util.Arrays;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.PasswordField;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.security.model.Password;

public class PasswordFormElement extends AbstractFormElement<char[]> {

	private final int maxLength;
	private final PasswordField textField;

	public PasswordFormElement(char[] key) {
		this(Keys.getProperty(key));
	}

	public PasswordFormElement(Password password) {
		this(password.getPassword());
	}

	public PasswordFormElement(PropertyInterface property) {
		super(property);
		this.maxLength = AnnotationUtil.getSize(property);
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
