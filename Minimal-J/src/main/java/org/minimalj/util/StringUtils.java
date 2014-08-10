package org.minimalj.util;


public class StringUtils {

	public static boolean equals(String s1, String s2) {
		if (s1 == null) {
			return s2 == null;
		} else {
			return s1.equals(s2);
		}
	}

	/**
	 * Returns true if one of the strings is equal to the first argument
	 * 
	 * @param string
	 * @param strings
	 * @return
	 */
	public static boolean equals(String string, String... strings) {
		if (string == null) {
			for (String s : strings) {
				if (s == null) return true;
			}
		} else {
			for (String s : strings) {
				if (string.equals(s)) return true;
			}
		}
		return false;
	}
	
	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	public static boolean isBlank(String s) {
		return s == null || s.trim().length() == 0;
	}
	
	public static String padLeft(String s, int length, char c) {
		if (s == null) s = "";
		while (s.length() < length) s = c + s;
		return s;
	}

	public static String padRight(String s, int length, char c) {
		if (s == null) s = "";
		while (s.length() < length) s = s + c;
		return s;
	}

	public static void appendLine(StringBuilder stringBuilder, String string) {
		if (!isEmpty(string)) {
			stringBuilder.append(string);
			stringBuilder.append("<br>");
		}
	}

	public static void appendLine(StringBuilder stringBuilder, Integer integer) {
		if (integer != null) {
			stringBuilder.append(integer);
			stringBuilder.append("<br>");
		}
	}

	public static void appendLine(StringBuilder stringBuilder, String... strings) {
		boolean first = true;
		for (String string : strings) {
			if (isEmpty(string)) continue;
			if (!first) stringBuilder.append(" "); else first = false;
			stringBuilder.append(string);
		}
		if (!first) stringBuilder.append("<br>");
	}
	
	public static void appendSeparated(StringBuilder stringBuilder, String... strings) {
		boolean first = true;
		for (String string : strings) {
			if (isEmpty(string)) continue;
			if (!first) stringBuilder.append(", "); else first = false;
			stringBuilder.append(string);
		}
	}
	
	
	public static String lowerFirstChar(String string) {
		if (string.length() > 1) {
			return string.substring(0, 1).toLowerCase() + string.substring(1);
		} else {
			return string.substring(0, 1);
		}
	}

	public static String upperFirstChar(String string) {
		if (string == null) throw new NullPointerException();
		if (string.length() == 0) throw new IllegalArgumentException("String must have a least one character");
		if (string.length() > 1) {
			return string.substring(0, 1).toUpperCase() + string.substring(1);
		} else {
			return string.substring(0, 1).toUpperCase();
		}
	}
	
	public static String toConstant(String string) {
		if (string == null) throw new NullPointerException();
		if (string.length() == 0) throw new IllegalArgumentException("String must have a least one character");
		StringBuffer s = new StringBuffer();
		for (int i = 0; i<string.length(); i++) {
			if (Character.isUpperCase(string.charAt(i))) {
				s.append('_');
			}
			s.append(Character.toUpperCase(string.charAt(i)));
		}
		return s.toString();
	}
	
	public static String toDbName(String string) {
		if (string == null) throw new NullPointerException();
		if (string.length() == 0) throw new IllegalArgumentException("String must have a least one character");
		StringBuffer s = new StringBuffer();
		s.append(Character.toUpperCase(string.charAt(0)));
		for (int i = 1; i<string.length(); i++) {
			if (Character.isUpperCase(string.charAt(i))) {
				s.append('_');
			}
			s.append(Character.toUpperCase(string.charAt(i)));
		}
		if (s.toString().equalsIgnoreCase("foreign")) return "FORIGN";
		return s.toString();
	}
	
	public static String escapeHTML(String s) {
		StringBuilder out = new StringBuilder(Math.max(16, s.length()));
		escapeHTML(out, s);
		return out.toString();
	}
	
	public static void escapeHTML(StringBuilder builder, String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
				builder.append("&#");
				builder.append((int) c);
				builder.append(';');
			} else {
				builder.append(c);
			}
		}
	}

}
