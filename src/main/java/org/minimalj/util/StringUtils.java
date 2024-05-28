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
	 * @param string the String the other should be compared to
	 * @param strings one ore more Strings to compare with the first parameter
	 * @return true if one of the second parameter String is equal
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
	
	public static int compare(String s1, String s2) {
		if (isEmpty(s1)) {
			return isEmpty(s2) ? -1 : 1;
		} else {
			return s1.compareTo(s2);
		}
	}
	
	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
	public static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
	
	public static String emptyIfNull(String s) {
		return s == null ? "" : s;
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
			stringBuilder.append(string).append('\n');
		}
	}

	public static void appendLine(StringBuilder stringBuilder, Integer integer) {
		if (integer != null) {
			stringBuilder.append(integer).append('\n');
		}
	}

	public static void appendLine(StringBuilder stringBuilder, String... strings) {
		boolean first = true;
		for (String string : strings) {
			if (isEmpty(string))
				continue;
			if (!first)
				stringBuilder.append(' ');
			else
				first = false;
			stringBuilder.append(string);
		}
		if (!first)
			stringBuilder.append('\n');
	}
	
	public static void appendSeparated(StringBuilder stringBuilder, String... strings) {
		boolean first = true;
		for (String string : strings) {
			if (isEmpty(string)) continue;
			if (!first) stringBuilder.append(", "); else first = false;
			stringBuilder.append(string);
		}
	}
	
	public static void trim(StringBuilder s) {
		while (s.length() > 0 && Character.isWhitespace(s.charAt(0))) {
			s.delete(0, 1);
		}
		while (Character.isWhitespace(s.charAt(s.length() - 1))) {
			s.delete(s.length() - 1, s.length());
		}
	}
	
	public static String lowerFirstChar(String string) {
		if (Character.isLowerCase(string.charAt(0))) {
			return string;
		} else {
			if (string.length() > 1) {
				return string.substring(0, 1).toLowerCase() + string.substring(1);
			} else {
				return string.substring(0, 1).toLowerCase();
			}
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
		StringBuilder s = new StringBuilder();
		string = string.replace("_", "__");
		for (int i = 0; i<string.length(); i++) {
			if (Character.isUpperCase(string.charAt(i))) {
				s.append('_');
			}
			s.append(Character.toUpperCase(string.charAt(i)));
		}
		return s.toString();
	}
	
	public static String toSnakeCase(String string) {
		if (StringUtils.isEmpty(string)) {
			return string;
		}
		StringBuilder s = new StringBuilder();
		s.append(string.charAt(0));
		for (int i = 1; i < string.length(); i++) {
			char ch = string.charAt(i);
			if (Character.isUpperCase(ch)) {
				s.append('_');
				ch = Character.toLowerCase(ch);
			}
			s.append(ch);
		}
		return s.toString();
	}
	
	public static String escapeHTML(String s) {
		if (!isEmpty(s)) {
			StringBuilder out = new StringBuilder(Math.max(16, s.length()));
			escapeHTML(out, s);
			return out.toString();
		} else {
			return s;
		}
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

	public static String stripHtml(String html) {
		if (html != null) {
		    return html.replaceAll("(?i)<.*?>", "");
		} else {
			return html;
		}
	}
}
