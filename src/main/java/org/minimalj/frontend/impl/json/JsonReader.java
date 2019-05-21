package org.minimalj.frontend.impl.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonReader {

	private static final Object OBJECT_END = "]";
	private static final Object ARRAY_END = "}";
	private static final Object COLON = ":";
	private static final Object COMMA = ",";

	private static final Map<Character, Character> escapes = new HashMap<>();

	static {
		escapes.put('"', '"');
		escapes.put('\\', '\\');
		escapes.put('/', '/');
		escapes.put('b', '\b');
		escapes.put('f', '\f');
		escapes.put('n', '\n');
		escapes.put('r', '\r');
		escapes.put('t', '\t');
	}

	private final InputStreamReader reader;

	private final String input;
	private int pos;

	private Character pushedBack;
	
	private StringBuilder builder = new StringBuilder();

	private JsonReader(String input) {
		this.input = input;
		this.reader = null;
	}

	private JsonReader(InputStreamReader reader) {
		this.input = null;
		this.reader = reader;
	}
	
	public static Object read(InputStream inputStream) {
		return read(new InputStreamReader(inputStream));
	}
	
	public static Object read(InputStreamReader reader) {
		return new JsonReader(reader).read();
	}
	
	public static Object read(String string) {
		return new JsonReader(string).read();
	}
	
	private char next() {
		if (pushedBack != null) {
			char res = pushedBack;
			pushedBack = null;
			return res;
		}
		if (input != null) {
			return input.charAt(pos++);
		} else {
			try {
				return (char) reader.read();
			} catch (IOException x) {
				throw new RuntimeException(x);
			}
		}
	}
	
	private void skip(int n) {
		if (pushedBack != null) {
			pushedBack = null;
			n = n -1;
		}
		if (input != null) {
			pos += n;
		} else {
			try {
				for (int i = 0; i<n; i++) {
					reader.read();
				}
			} catch (IOException x) {
				throw new RuntimeException(x);
			}
		}
	}

	private void pushBack(char c) {
		pushedBack = c;
	}

	private Object read() {
		char c = next();
		while (Character.isWhitespace(c)) {
			c = next();
		}
		switch (c) {
		case '"':
			return string();
		case '[':
			return array();
		case ']':
			return ARRAY_END;
		case ',':
			return COMMA;
		case '{':
			return object();
		case '}':
			return OBJECT_END;
		case ':':
			return COLON;
		case 't':
			skip(3); // true
			return Boolean.TRUE;
		case 'f':
			skip(4); // false
			return Boolean.FALSE;
		case 'n':
			skip(3); // null
			return null;
		default:
			if (Character.isDigit(c) || c == '-') {
				return number(c);
			}
		}
		throw new IllegalStateException(input);
	}

	private Object object() {
		Map<Object, Object> ret = new LinkedHashMap<>();
		Object key = read();
		while (key != OBJECT_END) {
			read(); // should be a colon
			ret.put(key, read());
			if (read() == COMMA) {
				key = read();
			} else {
				break;
			}
		}
		return ret;
	}

	private Object array() {
		List<Object> ret = new ArrayList<Object>();
		Object value = read();
		while (value != ARRAY_END) {
			ret.add(value);
			if (read() == COMMA) {
				value = read();
			} else {
				break;
			}
		}
		return ret;
	}

	private Object number(char c) {
		boolean isFloatingPoint = false;
		builder.setLength(0);

		if (c == '-') {
			c = add(c);
		}
		c = addDigits(c);
		if (c == '.') {
			c = add(c);
			c = addDigits(c);
			isFloatingPoint = true;
		}
		if (c == 'e' || c == 'E') {
			c = add(c);
			if (c == '+' || c == '-') {
				c = add(c);
			}
			c = addDigits(c);
			isFloatingPoint = true;
		}
		pushBack(c);
		
		String s = builder.toString();
		if (isFloatingPoint) {
			return Double.valueOf(s);
		} else {
			return Long.valueOf(s);
		}
	}

	private char addDigits(char c) {
		while (Character.isDigit(c)) {
			c = add(c);
		}
		return c;
	}

	private Object string() {
		builder.setLength(0);
		char c = next();
		while (c != '"') {
			if (c == '\\') {
				c = next();
				if (c == 'u') {
					c = add(unicode());
				} else {
					Object value = escapes.get(c);
					if (value != null) {
						c = add((Character) value);
					} else {
						c = next();
					}
				}
			} else {
				c = add(c);
			}
		}
		return builder.toString();
	}

	private char add(char cc) {
		builder.append(cc);
		return next();
	}
	
	private char unicode() {
		int value = 0;
		for (int i = 0; i < 4; ++i) {
			char c = next();
			if (c >= '0' && c <= '9') {
				value = (value << 4) + c - '0';
			} else if (c >= 'a' && c <= 'f') {
				value = (value << 4) + c - 'a' + 10;
			} else if (c >= 'A' && c <= 'F') {
				value = (value << 4) + c - 'A' + 10;
			} else {
				throw new IllegalArgumentException("char: " + c);
			}
		}
		return (char) value;
	}
}