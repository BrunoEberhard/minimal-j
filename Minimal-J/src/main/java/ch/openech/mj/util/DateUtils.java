package ch.openech.mj.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.joda.time.DateTimeFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.openech.mj.model.InvalidValues;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.annotation.Size;


public class DateUtils {
	private static final Logger logger = Logger.getLogger(DateUtils.class.getName());
	
	private static final DateTimeFieldType[] DATE_TIME_FIELD_TYPES_WITH_DAYS = new DateTimeFieldType[]{DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()};
	private static final DateTimeFieldType[] DATE_TIME_FIELD_TYPES_WITHOUT_DAYS = new DateTimeFieldType[]{DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()};

	public static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HH:mm");
	public static final DateTimeFormatter TIME_FORMAT_WITH_SECONDS = DateTimeFormat.forPattern("HH:mm:ss");
	public static final DateTimeFormatter TIME_FORMAT_WITH_MILIS = DateTimeFormat.forPattern("HH:mm:ss.SSS");

	public static final DateFormat dateFormatUS = new SimpleDateFormat("yyyy-MM-dd");
	
	private static class TrippleString {
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

	
	/**
	 * 
	 * @param text Date in format yyyy-mm-dd or yyyy-mm or yyyy
	 * @return <ul><li><code>null</code> if input text is null</li>
	 * <li>a partial if the input text is valid</li>
	 * <li>an InvalidValue if the input text is not valid</li></ul>
	 */
	public static Partial parsePartial(final String text) {
		if (text == null) return null;
		
		try {
			int length = text.length();
			if (length == 4) {
				return newPartial(text);
			} else if (length == 7) {
				return newPartial(completeYear(text.substring(0, 4)), text.substring(5, 7));
			} else if (length == 10) {
				return newPartial(completeYear(text.substring(0, 4)), text.substring(5, 7), text.substring(8, 10));
			} 
		} catch (IllegalFieldValueException x) {
			logger.fine("Return invalid value as could not parse " + text);
		}
		return InvalidValues.createInvalidPartial(text);
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

	public static Partial newPartial(String year) {
		return new Partial(DateTimeFieldType.year(), Integer.parseInt(year));
	}

	public static Partial newPartial(String year, String month) {
		return new Partial(DATE_TIME_FIELD_TYPES_WITHOUT_DAYS, new int[]{Integer.parseInt(year), Integer.parseInt(month)});
	}

	public static Partial newPartial(String year, String month, String day) {
		return new Partial(DATE_TIME_FIELD_TYPES_WITH_DAYS, //
				new int[]{Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day)});
	}

	public static String parseUS(String text) {
		if (text.length() == 2) {
			// Abgekürzter Fall, YY
			return completeYear(text);
		} else if (text.length() == 4) {
			// Abgekürzter Fall, YYYY
			return text;
		} else if (text.length() == 5) {
			// Abgekürzter Fall, YY-MM
			return pad(completeYear(text.substring(0,2)), 4) + "-" + pad(text.substring(3,5), 2);
		} else if (text.length() == 7) {
			// Abgekürzter Fall, YYYY-MM
			return pad(completeYear(text.substring(0,4)), 4) + "-" + pad(text.substring(5,7), 2);
		}

		if (text.length() == 6) text = text.substring(0, 2 ) + "-" + text.substring(2, 4) + "-" + text.substring(4);
		else if (text.length() == 8) text = text.substring(0, 4 ) + "-" + text.substring(4, 6) + "-" + text.substring(6);
		TrippleString trippleString = new TrippleString(text);
		return pad(completeYear(trippleString.s1), 4) + "-" + pad(trippleString.s2, 2) + "-" + pad(trippleString.s3, 2);
	}

	public static String formatCH(LocalDate date) {
		if (date == null) return null;
		return DateTimeFormat.shortDate().print(date);
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
	
	public static String pad(String s, int length) {
		while (s.length() < length) {
			s = "0" + s;
		}
		return s;
	}
	
	public static String completeYear(String year) {
		if (year.length() == 2 && year.compareTo("20") < 1) year = "20" + year;
		if (year.length() == 2) year = "19" + year;
		return year;
	}

	public static boolean isValueValidUS(String text) {
		return isValueValidUS(text, false);
	}
		
	public static boolean isValueValidUS(String text, boolean partialAllowed) {
		if (StringUtils.isBlank(text)) return false;
		
		int index = 0;
		while (index < text.length()) {
			if (index == 4 || index == 7) {
				if (text.charAt(index) != '-') return false;
			} else {
				if (!Character.isDigit(text.charAt(index))) return false;
			}
			index++;
		}
		if (partialAllowed) {
			if (text.length() == 4) text += "-01-01";
			else if (text.length() == 7) text += "-01";
		}
		
		dateFormatUS.setLenient(false);
		try {
			dateFormatUS.parse(text);
		} catch (ParseException pe) {
			return false;
		}
		return true;
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

	public static String formatPartial(ReadablePartial value) {
		if (value == null) return null;
		StringBuilder s = new StringBuilder();
		s.append(value.get(DateTimeFieldType.year()));
		if (s.length() > 4) throw new IllegalArgumentException(value.toString());
		while (s.length() < 4) {
			s.insert(0, "0");
		}
		if (value.isSupported(DateTimeFieldType.monthOfYear())) {
			s.append("-");
			int month = value.get(DateTimeFieldType.monthOfYear());
			if (month < 10) s.append("0");
			s.append(month);
			if (value.isSupported(DateTimeFieldType.dayOfMonth())) {
				s.append("-");
				int day = value.get(DateTimeFieldType.dayOfMonth());
				if (day < 10) s.append("0");
				s.append(day);
			}
		}
		return s.toString();
	}
	
	public static String formatPartialCH(ReadablePartial value) {
		if (value == null) return null;

		StringBuilder s = new StringBuilder();
		if (value.isSupported(DateTimeFieldType.dayOfMonth())) {
			int day = value.get(DateTimeFieldType.dayOfMonth());
			if (day < 10) s.append("0");
			s.append(day);
			s.append(".");
		}
		if (value.isSupported(DateTimeFieldType.monthOfYear())) {
			int month = value.get(DateTimeFieldType.monthOfYear());
			if (month < 10) s.append("0");
			s.append(month);
			s.append(".");
		}
		// TODO year < 1000 in DateUtils.formatPartialCH
		s.append(value.get(DateTimeFieldType.year()));
		return s.toString();
	}

	public static LocalDate convertToLocalDate(ReadablePartial value) {
		if (value == null) return null;
		if (value.isSupported(DateTimeFieldType.dayOfMonth())) {
			return new LocalDate(value.get(DateTimeFieldType.year()), //
					value.get(DateTimeFieldType.monthOfYear()), //
					value.get(DateTimeFieldType.dayOfMonth()));
		} else {
			return null;
		}
	}

}
