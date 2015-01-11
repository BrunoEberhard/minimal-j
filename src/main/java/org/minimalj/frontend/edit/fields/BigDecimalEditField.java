package org.minimalj.frontend.edit.fields;

import java.math.BigDecimal;
import java.util.Random;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.mock.Mocking;


public class BigDecimalEditField extends NumberEditField<BigDecimal> implements Mocking {

	public BigDecimalEditField(PropertyInterface property, int size, int decimalPlaces, boolean negative) {
		super(property, size, decimalPlaces, negative);
	}

	@Override
	public BigDecimal getObject() {
		String text = textField.getText();
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
