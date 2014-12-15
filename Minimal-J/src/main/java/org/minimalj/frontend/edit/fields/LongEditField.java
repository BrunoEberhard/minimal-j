package org.minimalj.frontend.edit.fields;

import java.util.Random;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DemoEnabled;


public class LongEditField extends NumberEditField<Long> implements DemoEnabled {

	public LongEditField(PropertyInterface property, int size, boolean negative) {
		super(property, size, 0, negative);
	}

	@Override
	public Long getObject() {
		String text = textField.getText();
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
	public void fillWithDemoData() {
		Random random = new Random();
		int max = 10;
		for (long i = 0; i<size; i++) max *= 10; // is there a exponential operator in Java?
		long value;
		do {
			value = random.nextLong();
		} while (value < 0 || value > max);
		if (!negative || random.nextBoolean()) {
			setObject(value);
		} else {
			setObject(-value);
		}
	}

}
