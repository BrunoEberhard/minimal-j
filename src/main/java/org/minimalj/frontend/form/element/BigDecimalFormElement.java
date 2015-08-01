package org.minimalj.frontend.form.element;

import java.math.BigDecimal;
import java.util.Random;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.mock.Mocking;


public class BigDecimalFormElement extends NumberFormElement<BigDecimal> implements Mocking {

	public BigDecimalFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}

	@Override
	public BigDecimal parse(String text) {
		if (text != null) {
			try {
				BigDecimal value = new BigDecimal(text);
				if (value.signum() < 0 && !this.signed) {
					return InvalidValues.createInvalidBigDecimal(text);
				}
				value = value.stripTrailingZeros();
				if (value.precision() > this.size) {
					return InvalidValues.createInvalidBigDecimal(text);
				}
				if (value.scale() > this.decimalPlaces) {
					return InvalidValues.createInvalidBigDecimal(text);
				}
				return value;
			} catch (NumberFormatException nfe) {
				return InvalidValues.createInvalidBigDecimal(text);
			}
		} else {
			return null;
		}
	}
	
	@Override
	public void mock() {
		Random random = new Random();
		StringBuilder s = new StringBuilder(size);
		for (int i = 0; i<size; i++) {
			s.append((char)('0' + random.nextInt(10)));
		}
		BigDecimal value = new BigDecimal(s.toString());
		value = value.movePointLeft(decimalPlaces);
		if (signed && random.nextBoolean()) {
			value = value.negate();
		}
		setValue(value);
	}

}