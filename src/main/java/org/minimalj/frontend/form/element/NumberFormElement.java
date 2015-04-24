package org.minimalj.frontend.form.element;

import org.minimalj.frontend.toolkit.ClientToolkit.InputType;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;


public abstract class NumberFormElement<T> extends FormatFormElement<T> {

	protected final boolean negative;
	protected final int size, decimalPlaces;
	
	protected NumberFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
		size = AnnotationUtil.getSize(property);
		decimalPlaces = AnnotationUtil.getDecimal(property);
		negative = AnnotationUtil.isNegative(property);
	}
	
	protected InputType getInputType() {
		return InputType.NUMBER;
	}

	@Override
	public void setObject(T number) {
		String text = null;
		if (number != null) {
			if (InvalidValues.isInvalid(number)) {
				text = InvalidValues.getInvalidValue(number);
			} else {
				text = number.toString();
			}
		}
		textField.setValue(text);
	}
	
	@Override
	protected String getAllowedCharacters(PropertyInterface property) {
		if (decimalPlaces > 0) {
			return negative ? "-0123456789." : "0123456789.";
		} else {
			return negative ? "-0123456789" : "0123456789";
		}
	}
	
	@Override
	protected int getAllowedSize(PropertyInterface property) {
		return size + (negative ? 1 : 0) + (decimalPlaces > 0 ? 1 : 0);
	}

}
