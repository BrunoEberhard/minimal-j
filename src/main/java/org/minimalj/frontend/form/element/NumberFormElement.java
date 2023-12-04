package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.Property;


public abstract class NumberFormElement<T> extends FormatFormElement<T> {

	protected final boolean signed;
	protected final int size, decimalPlaces, minDecimalPlaces;
	
	protected NumberFormElement(Property property, boolean editable) {
		super(property, editable);
		size = AnnotationUtil.getSize(property);
		decimalPlaces = AnnotationUtil.getDecimal(property);
		minDecimalPlaces = Math.min(decimalPlaces, AnnotationUtil.getMinDecimals(property));
		signed = AnnotationUtil.isSigned(property);
	}
	
	@Override
	protected InputType getInputType() {
		return InputType.NUMBER;
	}

	@Override
	public String render(T number) {
		return number != null ? number.toString() : null;
	}
	
	@Override
	protected String getAllowedCharacters(Property property) {
		if (decimalPlaces > 0) {
			return signed ? "-0123456789." : "0123456789.";
		} else {
			return signed ? "-0123456789" : "0123456789";
		}
	}
	
	@Override
	protected int getAllowedSize(Property property) {
		return size + (signed ? 1 : 0) + (decimalPlaces > 0 ? 1 : 0);
	}

}
