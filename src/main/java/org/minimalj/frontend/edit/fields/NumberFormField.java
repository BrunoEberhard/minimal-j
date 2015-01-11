package org.minimalj.frontend.edit.fields;

import java.math.BigDecimal;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;


public abstract class NumberFormField<T> implements FormField<T> {

	private final PropertyInterface property;
	
	protected final TextField textField;
	
	protected NumberFormField(PropertyInterface property) {
		this.property = property;
		this.textField = ClientToolkit.getToolkit().createReadOnlyTextField();
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	public static class IntegerFormField extends NumberFormField<Integer> {

		public IntegerFormField(PropertyInterface property) {
			super(property);
		}

		@Override
		public void setObject(Integer number) {
			String text = null;
			if (number != null) {
				if (InvalidValues.isInvalid(number)) {
					text = InvalidValues.getInvalidValue(number);
				} else {
					text = number.toString();
				}
			}
			textField.setText(text);
		}
	}

	public static class LongFormField extends NumberFormField<Long> {

		public LongFormField(PropertyInterface property) {
			super(property);
		}

		@Override
		public void setObject(Long number) {
			String text = null;
			if (number != null) {
				if (InvalidValues.isInvalid(number)) {
					text = InvalidValues.getInvalidValue(number);
				} else {
					text = number.toString();
				}
			}
			textField.setText(text);
		}
	}

	public static class BigDecimalFormField extends NumberFormField<BigDecimal> {

		public BigDecimalFormField(PropertyInterface property) {
			super(property);
		}

		@Override
		public void setObject(BigDecimal number) {
			String text = null;
			if (number != null) {
				if (InvalidValues.isInvalid(number)) {
					text = InvalidValues.getInvalidValue(number);
				} else {
					text = number.toString();
				}
			}
			textField.setText(text);
		}
	}

	
}
