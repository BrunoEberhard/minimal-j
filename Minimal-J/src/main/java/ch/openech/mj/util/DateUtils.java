package ch.openech.mj.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;


public class DateUtils {

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
	
	public static String parseCH(String text) {
		return parseCH(text, true);
	}
	
	public static String parseCH(String text, boolean partialAllowed) {
		if (!StringUtils.isEmpty(text)) {
			while (text.length() > 0 && !Character.isDigit(text.charAt(0))) {
				text = text.substring(1);
			}
		}
		if (StringUtils.isEmpty(text)) return "";
		while (text.length() > 0 && !Character.isDigit(text.charAt(text.length()-1))) {
			text = text.substring(0, text.length()-1);
		}
		if (StringUtils.isEmpty(text)) return "";
		
		// Nun hat der String sicher keinen Punkt mehr am Anfang oder Ende
		
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
			} else {
				return "";
			}
		}
		
		int length = text.length();
		
		if (!partialAllowed && length != 6 && length !=8) {
			return "";
		}
		
		for (int i = 0; i<text.length(); i++) {
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
		}
		
		return "";
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
	
}
