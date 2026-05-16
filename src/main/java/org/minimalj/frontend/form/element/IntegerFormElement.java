package org.minimalj.frontend.form.element;

import java.util.Random;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.Property;
import org.minimalj.util.StringUtils;
import org.minimalj.util.mock.Mocking;


public class IntegerFormElement extends NumberFormElement<Integer> implements Mocking {

	public IntegerFormElement(Integer key, boolean editable) {
		this(Keys.getProperty(key), editable);
	}
	
	public IntegerFormElement(Property property, boolean editable) {
		super(property, editable);
	}

	@Override
	public Integer parse(String text) throws NumberFormatException {
		if (!StringUtils.isEmpty(text)) {
			int value = Integer.parseInt(text);
			if (value < 0 && !this.signed) {
				throw new IllegalArgumentException("negative values not allowed");
			}
			int size = value < 0 ? text.length() - 1 : text.length();
			if (size <= this.size) {
				return value;
			} else {
				throw new IllegalArgumentException(text + " longer than " + this.size);
			}
		} else {
			return null;
		}
	}

	@Override
	public void mock() {
		Random random = new Random();
		int value = random.nextInt();
		if (size < Size.INTEGER) {
			int max = 10;
			for (int i = 1; i<size; i++) max = max * 10;
			value = value % max;
		}
		if (!signed && value < 0) {
			value = -value;
		}
		setValue(value);
	}

}
