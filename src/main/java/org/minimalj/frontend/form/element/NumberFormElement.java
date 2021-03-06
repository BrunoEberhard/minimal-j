package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;


public abstract class NumberFormElement<T> extends FormatFormElement<T> {

	protected final boolean signed;
	protected final int size, decimalPlaces;
	
	protected NumberFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
		size = AnnotationUtil.getSize(property);
		decimalPlaces = AnnotationUtil.getDecimal(property);
		signed = AnnotationUtil.isSigned(property);
	}
	
	@Override
	protected InputType getInputType() {
		return InputType.NUMBER;
	}

	@Override
	public String render(T number) {
		String string = null;
		if (number != null) {
			if (InvalidValues.isInvalid(number)) {
				string = InvalidValues.getInvalidValue(number);
			} else {
				string = number.toString();
			}
		}
		return string;
	}
	
	@Override
	protected String getAllowedCharacters(PropertyInterface property) {
		if (decimalPlaces > 0) {
			return signed ? "-0123456789." : "0123456789.";
		} else {
			return signed ? "-0123456789" : "0123456789";
		}
	}
	
	@Override
	protected int getAllowedSize(PropertyInterface property) {
		return size + (signed ? 1 : 0) + (decimalPlaces > 0 ? 1 : 0);
	}

}
