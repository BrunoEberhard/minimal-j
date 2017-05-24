package org.minimalj.frontend.form.element;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;
import org.minimalj.util.mock.MockDate;

public class LocalDateTimeFormElement extends FormatFormElement<LocalDateTime> {

	private static final boolean german = Locale.getDefault().getLanguage().equals(new Locale("de").getLanguage());
	private final DateTimeFormatter formatter;
	private final int size;
	
	public LocalDateTimeFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
		formatter = DateUtils.getTimeFormatter(property);
		Size sizeAnnotation = property.getAnnotation(Size.class);
		size = 11 + (sizeAnnotation != null ? sizeAnnotation.value() : Size.TIME_HH_MM);
	}
	
	@Override
	protected String getAllowedCharacters(PropertyInterface property) {
		return german ? "01234567890.: " : null;
	} 

	@Override
	protected int getAllowedSize(PropertyInterface property) {
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
					String[] parts = string.split(" ");
					return LocalDateTime.of(DateUtils.parse(parts[0]), LocalTime.parse(parts[1], formatter));
				}
			} catch (Exception x) {
				// ignore
			}
			return InvalidValues.createInvalidLocalDateTime(string);
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
				return DateUtils.format(value.toLocalDate()) + " " + formatter.format(value.toLocalTime());
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
