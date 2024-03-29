package org.minimalj.frontend.form.element;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;
import org.minimalj.util.mock.MockDate;

public class LocalDateFormElement extends FormatFormElement<LocalDate> {

	private static final boolean german = DateUtils.germanDateStyle();
	
	public LocalDateFormElement(Property property, boolean editable) {
		super(property, editable);
	}
	
	@Override
	protected String getAllowedCharacters(Property property) {
		return german ? "01234567890." : null;
	} 

	@Override
	protected int getAllowedSize(Property property) {
		return german ? 10 : 255;
	}
	
	@Override
	protected InputType getInputType() {
		return InputType.DATE;
	}

	@Override
	public LocalDate parse(String string) {
		if (string != null) {
			try {
				if (typed) {
					return LocalDate.parse(string);
				} else {
					return DateUtils.parse(string);
				}
			} catch (DateTimeParseException x) {
				return InvalidValues.createInvalidLocalDate(string);
			}
		} else {
			return null;
		}
	}
	
	@Override
	public String render(LocalDate value) {
		if (value != null) {
			return typed ? value.toString() : DateUtils.format(value);
		} else {
			return null;
		}
	}
	
	@Override
	public void mock() {
		setValue(MockDate.generateRandomDate());
	}
}