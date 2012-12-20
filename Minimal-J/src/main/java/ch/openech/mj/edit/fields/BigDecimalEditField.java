package ch.openech.mj.edit.fields;

import java.math.BigDecimal;
import java.util.Random;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.InvalidValues;
import ch.openech.mj.db.model.PropertyInterface;


public class BigDecimalEditField extends NumberEditField<BigDecimal> implements DemoEnabled {

	public BigDecimalEditField(PropertyInterface property, int size, int decimalPlaces, boolean negative) {
		super(property, size, decimalPlaces, negative);
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
	public void fillWithDemoData() {
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
