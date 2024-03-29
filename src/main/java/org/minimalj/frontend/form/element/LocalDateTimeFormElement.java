package org.minimalj.frontend.form.element;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;
import org.minimalj.util.mock.MockDate;

public class LocalDateTimeFormElement extends FormatFormElement<LocalDateTime> {

	private static final boolean german = DateUtils.germanDateStyle();
	private final int size;
	
	public LocalDateTimeFormElement(Property property, boolean editable) {
		super(property, editable);
		Size sizeAnnotation = property.getAnnotation(Size.class);
		size = 11 + (sizeAnnotation != null ? sizeAnnotation.value() : Size.TIME_HH_MM);
	}
	
	@Override
	protected String getAllowedCharacters(Property property) {
		return german ? "01234567890.: " : null;
	} 

	@Override
	protected int getAllowedSize(Property property) {
		return german ? size : 255;
	}
	
	@Override
	protected InputType getInputType() {
		return InputType.DATETIME;
	}

	@Override
	public LocalDateTime parse(String string) {
		if (string != null) {
			try {
				if (typed) {
					return LocalDateTime.parse(string);
				} else {
					return DateUtils.parseDateTime(string, getProperty());
				}
			} catch (DateTimeParseException x) {
				return InvalidValues.createInvalidLocalDateTime(string);
			}
		} else {
			return null;
		}
	}
	
	@Override
	public String render(LocalDateTime value) {
		if (InvalidValues.isInvalid(value)) {
			return typed ? InvalidValues.getInvalidValue(value) : null;
		} else if (value != null) {
			if (typed) {
				return value.toString();
			} else {
				return DateUtils.format(value, getProperty());
			}
		} else {
			return null;
		}
	}
	
	@Override
	public void mock() {
		setValue(LocalDateTime.of(MockDate.generateRandomDate(), MockDate.generateRandomTime(Size.TIME_HH_MM)));
	}
}
