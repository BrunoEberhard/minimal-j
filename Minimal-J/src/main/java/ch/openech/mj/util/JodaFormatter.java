package ch.openech.mj.util;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class JodaFormatter {

	private DateTimeFormatter localDateFormatter;
	private DateTimeFormatter localTimeFormatter;
	private DateTimeFormatter localDateTimeFormatter;
	
	public String format(Object value) {
		if (value == null) return null;
		if (value instanceof LocalDate) {
			if (localDateFormatter == null) {
				localDateFormatter = DateTimeFormat.mediumDate();
			}
			return localDateFormatter.print((LocalDate) value);
		}
		if (value instanceof LocalTime) {
			if (localTimeFormatter == null) {
				localTimeFormatter = DateTimeFormat.mediumTime();
			}
			return localTimeFormatter.print((LocalDate) value);
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
