package org.minimalj.frontend.form.element;

import java.util.Random;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.mock.Mocking;


public class IntegerFormElement extends NumberFormElement<Integer> implements Mocking {

	public IntegerFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}

	@Override
	public Integer parse(String text) {
		if (text != null) {
			try {
				int value = Integer.parseInt(text);
				int size = value < 0 ? text.length() - 1 : text.length();
				if (size <= this.size) {
					return value;
				} else {
					return InvalidValues.createInvalidInteger(text);
				}
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
			setValue(value);
		} else {
			setValue(-value);
		}
	}

}
