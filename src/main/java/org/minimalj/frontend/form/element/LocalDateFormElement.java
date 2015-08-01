package org.minimalj.frontend.form.element;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;
import org.minimalj.util.mock.MockDate;

public class LocalDateFormElement extends FormatFormElement<LocalDate> {

	public LocalDateFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}
	
	@Override
	protected String getAllowedCharacters(PropertyInterface property) {
		return "01234567890.-";
	} 

	@Override
	protected int getAllowedSize(PropertyInterface property) {
		return 10;
	}

	@Override
	public LocalDate parse(String string) {
		try {
			return DateUtils.parse(string);
		} catch (DateTimeParseException x) {
			return InvalidValues.createInvalidLocalDate(string);
		}
	}
	
	@Override
	public String render(LocalDate value) {
		if (InvalidValues.isInvalid(value)) {
			return InvalidValues.getInvalidValue(value);
		} else if (value != null) {
			return DateUtils.format(value);
		} else {
			return null;
		}
	}
	
	@Override
	public void mock() {
		setValue(MockDate.generateRandomDate());
	}
}