package org.minimalj.frontend.form.element;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;
import org.minimalj.util.StringUtils;
import org.minimalj.util.mock.MockDate;

public class LocalTimeFormElement extends FormatFormElement<LocalTime> {
	private final DateTimeFormatter formatter;
	private final int size;
	
	public LocalTimeFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
		formatter = DateUtils.getTimeFormatter(property);
		size = property.getAnnotation(Size.class).value();
	}
	
	@Override
	protected String getAllowedCharacters(PropertyInterface property) {
		int size = property.getAnnotation(Size.class).value();
		if (size > Size.TIME_WITH_SECONDS) {
			return "01234567890:.";
		} else {
			return "01234567890:";
		}
	} 

	@Override
	protected int getAllowedSize(PropertyInterface property) {
		int size = property.getAnnotation(Size.class).value();
		switch (size) {
		case Size.TIME_HH_MM: return 5;
		case Size.TIME_WITH_SECONDS: return 8;
		case Size.TIME_WITH_MILLIS: return 12;
		default: throw new IllegalArgumentException(String.valueOf(size));
		}
	}

	@Override
	public LocalTime getObject() {
		String text = textField.getValue();
		if (text != null) {
			try {
				return LocalTime.parse(text, formatter);
			} catch (IllegalArgumentException iae) {
				return InvalidValues.createInvalidLocalTime(text);
			}
		} else {
			return null;
		}
	}
	
	@Override
	public void setObject(LocalTime value) {
		if (InvalidValues.isInvalid(value)) {
			String text = InvalidValues.getInvalidValue(value);
			textField.setValue(text);
		} else if (value != null) {
			String text = formatter.format(value);
			if (!StringUtils.equals(textField.getValue(), text)) {
				textField.setValue(text);
			}
		} else {
			textField.setValue(null);
		}
	}

	@Override
	public void mock() {
		setObject(MockDate.generateRandomTime(size));
	}

}