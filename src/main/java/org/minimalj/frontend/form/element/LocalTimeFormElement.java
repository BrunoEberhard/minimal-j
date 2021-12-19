package org.minimalj.frontend.form.element;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.DateUtils;
import org.minimalj.util.mock.MockDate;

public class LocalTimeFormElement extends FormatFormElement<LocalTime> {
	private final DateTimeFormatter formatter;
	private final int size;
	private boolean upperEnd;
	
	public LocalTimeFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
		formatter = DateUtils.getTimeFormatter(property);
		Size sizeAnnotation = property.getAnnotation(Size.class);
		size = sizeAnnotation != null ? sizeAnnotation.value() : Size.TIME_HH_MM;
	}
	
	@Override
	protected String getAllowedCharacters(PropertyInterface property) {
		if (size > Size.TIME_WITH_SECONDS) {
			return "01234567890:.";
		} else {
			return "01234567890:";
		}
	}

	@Override
	protected InputType getInputType() {
		return InputType.TIME;
	}

	@Override
	protected int getAllowedSize(PropertyInterface property) {
		return DateUtils.getTimeSize(property);
	}

	public void setUpperEnd(boolean upperEnd) {
		this.upperEnd = upperEnd;
	}
	
	@Override
	public LocalTime parse(String string) {
		return DateUtils.parseTime(string, upperEnd);
	}
	
	@Override
	public String render(LocalTime value) {
		if (value != null) {
			return formatter.format(value);
		} else {
			return null;
		}
	}

	@Override
	public void mock() {
		setValue(MockDate.generateRandomTime(size));
	}

}