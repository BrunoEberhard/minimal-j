package org.minimalj.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.minimalj.application.Configuration;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.InvalidValues;


public class DateUtils {
	// for the next 5 year convert 01.01.xx to 01.01.20xx. Else to 01.01.19xx
	private static final String SWITCH_YEAR = Configuration.get("MjSwitchYear", String.valueOf(LocalDate.now().getYear() % 100 + 5));
	
	private static final Map<Locale, DateTimeFormatter> dateFormatByLocale = new HashMap<>();
	private static final Map<Locale, Boolean> germanDateStyle = new HashMap<>();
	
	public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
	public static final DateTimeFormatter TIME_FORMAT_WITH_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static final DateTimeFormatter TIME_FORMAT_WITH_MILIS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	public static final DateTimeFormatter TIME_PARSE = DateTimeFormatter.ofPattern("H:mm");
	public static final DateTimeFormatter TIME_PARSE_WITH_SECONDS = DateTimeFormatter.ofPattern("H:mm:ss");
	public static final DateTimeFormatter TIME_PARSE_WITH_MILIS = DateTimeFormatter.ofPattern("H:mm:ss.SSS");

	private static Locale forcedLocale;
	
	static {
		String languageTage = Configuration.get("MjDateLocale", null);
		if (!StringUtils.isEmpty(languageTage)) {
			forcedLocale = Locale.forLanguageTag(languageTage);
		}
	}
	
	private static Locale getLocale() {
		return forcedLocale != null ? forcedLocale : LocaleContext.getCurrent();
	}
	
	private static DateTimeFormatter getDateTimeFormatter() {
		Locale locale = getLocale();
		if (!dateFormatByLocale.containsKey(locale)) {
			DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendLocalized(FormatStyle.MEDIUM, null).toFormatter(locale);
			dateFormatByLocale.put(locale, formatter);
			String localizedDatePattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM, null, IsoChronology.INSTANCE, locale);
			germanDateStyle.put(locale,	StringUtils.equals(localizedDatePattern, "dd.MM.yyyy", "dd.MM.y"));
		}
		return dateFormatByLocale.get(locale);
	}
	
	private static class TrippleString {
		private String s1, s2, s3;
		
		private TrippleString(String text) {
			int index = 0;
			s1 = "";
			while (index < text.length() && Character.isDigit(text.charAt(index))) {
				s1 += text.charAt(index);
				index++;
			}
			while (index < text.length() && !Character.isDigit(text.charAt(index))) {
				index++;
			}
			s2 = "";
			while (index < text.length() && Character.isDigit(text.charAt(index))) {
				s2 += text.charAt(index);
				index++;
			}
			while (index < text.length() && !Character.isDigit(text.charAt(index))) {
				index++;
			}
			s3= "";
			while (index < text.length() && Character.isDigit(text.charAt(index))) {
				s3+= text.charAt(index);
				index++;
			}
		}
	}
	
	/**
	 * Converts a CH - Date String in a yyyy-mm-dd String. The conversion
	 * is very lenient and tries to convert as long as its somehow clear
	 * what the user may have wanted.
	 * 
	 * @param inputText The input text. Maybe empty or <code>null</code>.
	 * @param partialAllowed false if the inputText has to be a complete date with month and day
	 * @return null if the input text is empty (<code>null</code> or length 0). An empty String
	 * if the input text doesn't fit a date or a String in format yyyy-mm-dd (or yyyy-mm or even
	 * yyyy if partial allowed)
	 */
	public static String parseCH(String inputText, boolean partialAllowed) {
		if (StringUtils.isEmpty(inputText)) return null;
		String text = cutNonDigitsAtBegin(inputText);
		if (StringUtils.isEmpty(text)) return "";
		text = cutNonDigitsAtEnd(text);
		if (StringUtils.isEmpty(text)) return "";
		
		// Nun hat der String sicher keinen Punkt mehr am Anfang oder Ende
		
		if (inputText.contains(".")) {
			return parseCHWithDot(inputText, partialAllowed);
		} else {
			return parseCHWithoutDot(inputText, partialAllowed);
		}
	}
	
	private static String parseCHWithDot(String text, boolean partialAllowed) {
		TrippleString trippleString = new TrippleString(text);
		if (trippleString.s1 != null && trippleString.s1.length() > 2) {
			if (!partialAllowed || !StringUtils.isBlank(trippleString.s2)) return "";
		}
		if (trippleString.s2 != null && trippleString.s2.length() > 2) {
			if (!partialAllowed || !StringUtils.isBlank(trippleString.s3)) return "";
		}
		if (trippleString.s3 != null && trippleString.s3.length() > 4) return "";
		
		if (!StringUtils.isBlank(trippleString.s3)) {
			return pad(completeYear(trippleString.s3), 4) + "-" + pad(trippleString.s2, 2) + "-" + pad(trippleString.s1, 2);
		} else if (!StringUtils.isBlank(trippleString.s2)) {
			if (partialAllowed) {
				return pad(completeYear(trippleString.s2), 4) + "-" + pad(trippleString.s1, 2);		
			} else {
				return "";
			}
		} else if (!StringUtils.isBlank(trippleString.s1) && trippleString.s1.length() <= 4) {
			if (partialAllowed) {
				return pad(completeYear(trippleString.s1), 4);		
			}
		}
		return "";
	}

	private static String parseCHWithoutDot(String text, boolean partialAllowed) {
		int length = text.length();
		
		if (!partialAllowed && length != 6 && length !=8) {
			return "";
		}
		
		for (int i = 0; i<length; i++) {
			if (!Character.isDigit(text.charAt(i))) return "";
		}
		
		if (length == 4) {
			// Abgekürzter Fall, YYYY
			return text;
		} else if (length == 6) {
			// Fall, DDMMYY
			return completeYear(text.substring(4, 6)) + "-" + text.substring(2, 4) + "-" + text.substring(0, 2);
		} else if (length == 8) {
			// DDMMYYYY
			return completeYear(text.substring(4, 8)) + "-" + text.substring(2, 4) + "-" + text.substring(0, 2);
		} else {
			return "";
		}
	}

	private static String cutNonDigitsAtBegin(String text) {
		while (text.length() > 0 && !Character.isDigit(text.charAt(0))) {
			text = text.substring(1);
		}
		return text;
	}

	private static String cutNonDigitsAtEnd(String text) {
		while (text.length() > 0 && !Character.isDigit(text.charAt(text.length()-1))) {
			text = text.substring(0, text.length()-1);
		}
		return text;
	}

	private static String completeYear(String year) {
		if (year.length() == 2 && year.compareTo(SWITCH_YEAR) < 1) year = "20" + year;
		if (year.length() == 2) year = "19" + year;
		return year;
	}


	private static String pad(String s, int length) {
		return StringUtils.padLeft(s, length, '0');
	}

	/**
	 * 
	 * @param date a local date or <code>null</code>
	 * @return the formatted date in FormatStyle MEDIUM
	 */
	public static String format(LocalDate date) {
		if (date == null) return null;
		return getDateTimeFormatter().format(date);
	}

	/**
	 * Tries to be a little bit more clever than the normal parsing. Accept dates
	 * like 1.2.2013 or 010214
	 * 
	 * @param date date as a String or <code>null</code>
	 * @return LocalDate date object (valid or invalid) or <code>null</code>
	 */
	public static LocalDate parse(String date) {
		if (StringUtils.isEmpty(date)) return null;
		try {
			return parse_(date);
		} catch (DateTimeParseException x) {
			return InvalidValues.createInvalidLocalDate(date);
		}
	}

	// framework internal, only used by LocalDateTimeFormElement
	public static LocalDate parse_(String date) {
		DateTimeFormatter dateTimeFormatter = getDateTimeFormatter();
		if (germanDateStyle()) {
			date = parseCH(date, false);
			return LocalDate.parse(date);
		} else {
			return LocalDate.parse(date, dateTimeFormatter);
		}
	}

	public static boolean germanDateStyle() {
		getDateTimeFormatter();
		return germanDateStyle.get(getLocale());
	}
	
	/**
	 * @param value date in format yyyy-mm-dd
	 * @return date in format dd.mm.yyyy
	 */
	public static String format(String value) {
		if (StringUtils.isEmpty(value))
			return "";
		if (InvalidValues.isInvalid(value))
			return InvalidValues.getInvalidValue(value);
		
		TrippleString trippleString = new TrippleString(value);
		if (!StringUtils.isEmpty(trippleString.s3)) {
			return trippleString.s3 + "." + trippleString.s2 + "." + trippleString.s1;
		} else if (!StringUtils.isEmpty(trippleString.s2)) {
			return trippleString.s2 + "." + trippleString.s1;
		} else {
			return trippleString.s1;
		}
	}	
	
	private static SimpleDateFormat xsdDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	public static String formatXsd(Date date) {
		String s = xsdDateTimeFormat.format(date);
		StringBuilder sb = new StringBuilder(s);
		sb.insert(22, ':');
		return sb.toString();
	}
	
	public static Date parseXsd(String xmlDateTime) throws ParseException {
		if (xmlDateTime.length() < 25) {
			throw new ParseException("Date not in expected xml datetime format", 0);
		}
		StringBuilder sb = new StringBuilder(xmlDateTime);
		sb.deleteCharAt(22);
		return xsdDateTimeFormat.parse(sb.toString());
	}

	public static int getTimeSize(Property property) {
		Size size = property.getAnnotation(Size.class);
		if (size == null) {
			return Size.TIME_HH_MM;
		}
		return size.value();
	}
	
	public static DateTimeFormatter getTimeFormatter(Property property) {
		if (property == null) {
			return TIME_FORMAT;
		}
		Size size = property.getAnnotation(Size.class);
		if (size == null) {
			return TIME_FORMAT;
		}
		if (size.value() == Size.TIME_HH_MM) return TIME_FORMAT;
		else if (size.value() == Size.TIME_WITH_SECONDS) return TIME_FORMAT_WITH_SECONDS;
		else if (size.value() == Size.TIME_WITH_MILLIS) return TIME_FORMAT_WITH_MILIS;
		else {
			return TIME_FORMAT;
		}
	}
	
	public static DateTimeFormatter getTimeParser(Property property) {
		if (property == null) {
			return TIME_PARSE;
		}
		Size size = property.getAnnotation(Size.class);
		if (size == null) {
			return TIME_PARSE;
		}
		if (size.value() == Size.TIME_HH_MM) return TIME_PARSE;
		else if (size.value() == Size.TIME_WITH_SECONDS) return TIME_PARSE_WITH_SECONDS;
		else if (size.value() == Size.TIME_WITH_MILLIS) return TIME_PARSE_WITH_MILIS;
		else {
			return TIME_PARSE;
		}
	}
	
	public static String format(LocalDateTime localDateTime, Property property) {
		if (localDateTime != null) {
			String date = DateUtils.format(localDateTime.toLocalDate());
			String time = DateUtils.getTimeFormatter(property).format(localDateTime);
			return date + " " + time; 
		} else {
			return null;
		}
	}
	
	public static LocalDateTime parseDateTime(String string, Property property) {
		if (!StringUtils.isEmpty(string)) {
			DateTimeFormatter parser = DateUtils.getTimeParser(property);
			String[] parts = string.split(" ");
			return LocalDateTime.of(DateUtils.parse_(parts[0]), LocalTime.parse(parts[1], parser));
		} else {
			return null;
		}
	}
	
	//
	
	public static LocalDateTime parseDateTime(String s, Boolean upperEnd) {
		if (!StringUtils.isEmpty(s)) {
			s = s.trim();
			int pos = s.indexOf(" ");
			if (pos > 0) {
				String[] parts = s.split(" ");
				LocalDate date = parseDate(parts[0], upperEnd);
				LocalTime time = parseTime(parts[1], upperEnd);
				if (date != null && !InvalidValues.isInvalid(date)) {
					if (time != null && !InvalidValues.isInvalid(time)) {
						return LocalDateTime.of(date, time);
					} else {
						return LocalDateTime.of(date, upperEnd ? LocalTime.MAX : LocalTime.MIN);
					}
				}
			} else {
				LocalDate date = parseDate(s, upperEnd);
				if (date != null && !InvalidValues.isInvalid(date)) {
					return LocalDateTime.of(date, upperEnd ? LocalTime.MAX : LocalTime.MIN);
				}
			}
		}
		return null;
	}

	public static LocalDate parseDate(String s, Boolean upperEnd) {
		if (!StringUtils.isEmpty(s)) {
			if (s.length() == 4) {
				try {
					Year year = Year.parse(s);
					if (upperEnd) {
						return LocalDate.of(year.getValue(), 12, 31);
					} else {
						return LocalDate.of(year.getValue(), 1, 1);
					}
				} catch (DateTimeParseException ignored) {
					//
				}
			}
			return DateUtils.parse(s);
		}
		return null;
	}

	public static LocalTime parseTime(String s, Boolean upperEnd) {
		if (!StringUtils.isEmpty(s)) {
			try {
				LocalTime time = LocalTime.parse(s, DateUtils.TIME_PARSE_WITH_MILIS);
				if (upperEnd) {
					return LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), time.getNano() + 999999);
				} else {
					return time;
				}
			} catch (DateTimeParseException ignored) {
				//
			}
			try {
				LocalTime time = LocalTime.parse(s, DateUtils.TIME_PARSE_WITH_SECONDS);
				if (upperEnd) {
					return LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), 999999999);
				} else {
					return time;
				}
			} catch (DateTimeParseException ignored) {
				//
			}
			try {
				LocalTime time = LocalTime.parse(s, DateUtils.TIME_PARSE);
				if (upperEnd) {
					return LocalTime.of(time.getHour(), time.getMinute(), 59, 999999999);
				} else {
					return time;
				}
			} catch (DateTimeParseException ignored) {
				//
			}
			return InvalidValues.createInvalidLocalTime(s);
		}
		return null;
	}

}
