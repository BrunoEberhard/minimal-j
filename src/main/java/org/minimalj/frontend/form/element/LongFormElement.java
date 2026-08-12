package org.minimalj.frontend.form.element;

import java.util.Random;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.Property;
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
	public Long parse(String text) throws NumberFormatException {
		if (!StringUtils.isEmpty(text)) {
			return Long.parseLong(text);
		} else if (getProperty().getClazz() == Long.TYPE) {
			throw new IllegalArgumentException("null not allowed for java primitive");
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
