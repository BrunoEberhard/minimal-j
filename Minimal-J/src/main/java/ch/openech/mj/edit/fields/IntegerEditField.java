package ch.openech.mj.edit.fields;

import java.util.Random;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.model.InvalidValues;
import ch.openech.mj.model.PropertyInterface;


public class IntegerEditField extends NumberEditField<Integer> implements DemoEnabled {

	public IntegerEditField(PropertyInterface property, int size, boolean negative) {
		super(property, size, 0, negative);
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

	@Override
	public Integer getObject() {
		String text = textField.getText();
		if (text != null) {
			try {
				return Integer.parseInt(text);
			} catch (NumberFormatException nfe) {
				return InvalidValues.createInvalidInteger(text);
			}
		} else {
			return null;
		}
	}

	@Override
	public void fillWithDemoData() {
		Random random = new Random();
		int max = 10;
		for (int i = 0; i<size; i++) max *= 10; // is there a exponential operator in Java?
		int value = random.nextInt(max);
		if (!negative || random.nextBoolean()) {
			setObject(value);
		} else {
			setObject(-value);
		}
	}

}
