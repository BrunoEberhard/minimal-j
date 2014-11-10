package org.minimalj.frontend.edit.fields;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.minimalj.autofill.DateGenerator;
import org.minimalj.model.InvalidValues;
import org.minimalj.model.PropertyInterface;
import org.minimalj.util.DateUtils;
import org.minimalj.util.StringUtils;

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
		String fieldText = textField.getText();
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
			textField.setText(text);
		} else if (value != null) {
			String text = DateUtils.format(value);
			if (!StringUtils.equals(textField.getText(), text)) {
				textField.setText(text);
			}
		} else {
			textField.setText(null);
		}
	}
	
	@Override
	public void fillWithDemoData() {
		setObject(DateGenerator.generateRandomDate());
	}
}