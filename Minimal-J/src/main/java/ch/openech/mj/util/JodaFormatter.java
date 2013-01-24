package ch.openech.mj.util;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.openech.mj.model.PropertyInterface;

public class JodaFormatter {

	private DateTimeFormatter localDateFormatter;
	private DateTimeFormatter localDateTimeFormatter;
	
	public String format(Object value, PropertyInterface property) {
		if (value == null) return null;
		if (value instanceof LocalDate) {
			if (localDateFormatter == null) {
				localDateFormatter = DateTimeFormat.mediumDate();
			}
			return localDateFormatter.print((LocalDate) value);
		}
		if (value instanceof LocalTime) {
			return DateUtils.getTimeFormatter(property).print((LocalTime) value);
		}
		if (value instanceof LocalDateTime) {
			if (localDateTimeFormatter == null) {
				localDateTimeFormatter = DateTimeFormat.mediumDateTime();
			}
			return localDateTimeFormatter.print((LocalDateTime) value);
		}
		return value.toString();
	}
	
}
