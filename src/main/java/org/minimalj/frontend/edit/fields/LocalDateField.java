package org.minimalj.frontend.edit.fields;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;
import org.minimalj.util.StringUtils;
import org.minimalj.util.mock.MockDate;

public class LocalDateField extends TextFormatField<LocalDate> {

	public LocalDateField(PropertyInterface property, boolean editable) {
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
	public LocalDate getObject() {
		String fieldText = textField.getValue();
		try {
			return DateUtils.parse(fieldText);
		} catch (DateTimeParseException x) {
			return InvalidValues.createInvalidLocalDate(fieldText);
		}
	}
	
	@Override
	public void setObject(LocalDate value) {
		if (InvalidValues.isInvalid(value)) {
			String text = InvalidValues.getInvalidValue(value);
			textField.setValue(text);
		} else if (value != null) {
			String text = DateUtils.format(value);
			if (!StringUtils.equals(textField.getValue(), text)) {
				textField.setValue(text);
			}
		} else {
			textField.setValue(null);
		}
	}
	
	@Override
	public void mock() {
		setObject(MockDate.generateRandomDate());
	}
}