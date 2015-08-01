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
	public Long parse(String text) {
		if (text != null) {
			try {
				long value = Long.parseLong(text);
				if (value < 0 && !this.negative) {
					return InvalidValues.createInvalidLong(text);
				}
				int size = value < 0 ? text.length() - 1 : text.length();
				if (size <= this.size) {
					return value;
				} else {
					return InvalidValues.createInvalidLong(text);
				}
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
