package org.minimalj.frontend.edit.fields;

import java.util.Random;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.mock.Mocking;


public class IntegerEditField extends NumberEditField<Integer> implements Mocking {

	public IntegerEditField(PropertyInterface property, int size, boolean negative) {
		super(property, size, 0, negative);
	}

	@Override
	public Integer getObject() {
		String text = textField.getValue();
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
	public void mock() {
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
