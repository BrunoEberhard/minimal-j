package org.minimalj.frontend.form.element;

import java.util.Random;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.StringUtils;
import org.minimalj.util.mock.Mocking;


public class LongFormElement extends NumberFormElement<Long> implements Mocking {

	public LongFormElement(Long key, boolean editable) {
		this(Keys.getProperty(key), editable);
	}
	
	public LongFormElement(Property property, boolean editable) {
		super(property, editable);
	}

	@Override
	public Long parse(String text) {
		if (!StringUtils.isEmpty(text)) {
			try {
				long value = Long.parseLong(text);
				if (value < 0 && !this.signed) {
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
		long value = random.nextLong();
		if (size < Size.LONG) {
			long max = 10;
			for (int i = 1; i<size; i++) max = max * 10;
			value = value % max;
		}
		if (!signed && value < 0) {
			value = -value;
		}
		setValue(value);
	}

}
