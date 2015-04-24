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
	public BigDecimal getObject() {
		String text = textField.getValue();
		if (text != null) {
			try {
				return new BigDecimal(text);
			} catch (NumberFormatException nfe) {
				return InvalidValues.createInvalidBigDecimal(text);
			}
		} else {
			return null;
		}
	}

	@Override
	public void mock() {
		// TODO check for valid ranges for size-decimalPlaces
		Random random = new Random();
		double value = random.nextDouble() * (10 ^ (size - decimalPlaces));
		if (!negative || random.nextBoolean()) {
			setObject(BigDecimal.valueOf(value));
		} else {
			setObject(BigDecimal.valueOf(-value));
		}
	}

}
