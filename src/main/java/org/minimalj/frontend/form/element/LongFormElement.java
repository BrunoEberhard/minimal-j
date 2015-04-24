package org.minimalj.frontend.form.element;

import java.util.Random;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.mock.Mocking;


public class LongFormElement extends NumberFormElement<Long> implements Mocking {

	public LongFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}

	@Override
	public Long getValue() {
		String text = textField.getValue();
		if (text != null) {
			try {
				return Long.parseLong(text);
			} catch (NumberFormatException nfe) {
				return InvalidValues.createInvalidLong(text);
			}
		} else {
			return null;
		}
	}

	@Override
	public void mock() {
		Random random = new Random();
		int max = 10;
		for (long i = 0; i<size; i++) max *= 10; // is there a exponential operator in Java?
		long value;
		do {
			value = random.nextLong();
		} while (value < 0 || value > max);
		if (!negative || random.nextBoolean()) {
			setValue(value);
		} else {
			setValue(-value);
		}
	}

}
