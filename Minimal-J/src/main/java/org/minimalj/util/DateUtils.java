package org.minimalj.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.minimalj.model.InvalidValues;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.Size;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.TemporalAccessor;


public class DateUtils {
	private static final Logger logger = Logger.getLogger(DateUtils.class.getName());
	
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

	public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
	public static final DateTimeFormatter TIME_FORMAT_WITH_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static final DateTimeFormatter TIME_FORMAT_WITH_MILIS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	public static final DateFormat dateFormatUS = new SimpleDateFormat("yyyy-MM-dd");
	
	public static class TrippleString {
		public String s1, s2, s3;
		
		public TrippleString(String text) {
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
		
		if (inputText.indexOf(".") > -1) {
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
		if (year.length() == 2 && year.compareTo("20") < 1) year = "20" + year;
		if (year.length() == 2) year = "19" + year;
		return year;
	}


	private static String pad(String s, int length) {
		return StringUtils.padLeft(s, length, '0');
	}

	public static String formatCH(LocalDate date) {
		if (date == null) return null;
		return DATE_FORMATTER.format(date);
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
	
	public static DateTimeFormatter getTimeFormatter(PropertyInterface property) {
		Size size = property.getAnnotation(Size.class);
		if (size == null) {
			logger.warning(property.getFieldPath() + " has no size for LocalTime. Use default.");
			return TIME_FORMAT;
		}
		if (size.value() == Size.TIME_HH_MM) return TIME_FORMAT;
		else if (size.value() == Size.TIME_WITH_SECONDS) return TIME_FORMAT_WITH_SECONDS;
		else if (size.value() == Size.TIME_WITH_MILLIS) return TIME_FORMAT_WITH_MILIS;
		else {
			logger.severe(property.getFieldPath() + " has wrong size for LocalTime. Use default.");
			return TIME_FORMAT;
		}
	}

	public static String formatPartial(TemporalAccessor value) {
		if (value == null) return null;
		StringBuilder s = new StringBuilder();
		s.append(value.get(ChronoField.YEAR));
		if (s.length() > 4) throw new IllegalArgumentException(value.toString());
		while (s.length() < 4) {
			s.insert(0, "0");
		}
		if (value.isSupported(ChronoField.MONTH_OF_YEAR)) {
			s.append("-");
			int month = value.get(ChronoField.MONTH_OF_YEAR);
			if (month < 10) s.append("0");
			s.append(month);
			if (value.isSupported(ChronoField.DAY_OF_MONTH)) {
				s.append("-");
				int day = value.get(ChronoField.DAY_OF_MONTH);
				if (day < 10) s.append("0");
				s.append(day);
			}
		}
		return s.toString();
	}
	
	public static String formatPartialCH(TemporalAccessor value) {
		if (value == null) return null;

		StringBuilder s = new StringBuilder();
		if (value.isSupported(ChronoField.DAY_OF_MONTH)) {
			int day = value.get(ChronoField.DAY_OF_MONTH);
			if (day < 10) s.append("0");
			s.append(day);
			s.append(".");
		}
		if (value.isSupported(ChronoField.MONTH_OF_YEAR)) {
			int month = value.get(ChronoField.MONTH_OF_YEAR);
			if (month < 10) s.append("0");
			s.append(month);
			s.append(".");
		}
		// TODO year < 1000 in DateUtils.formatPartialCH
		s.append(value.get(ChronoField.YEAR));
		return s.toString();
	}

}
