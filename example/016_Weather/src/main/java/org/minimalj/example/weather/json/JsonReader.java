package org.minimalj.example.weather.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;

// extended version of the Minimal-J class. When finished could be moved to Minimal-J
// Missing: LocalDate, LocalTime, some Tests
public class JsonReader {

	private static final Object OBJECT_END = new Object();
	private static final Object ARRAY_END = new Object();
	private static final Object COLON = new Object();
	private static final Object COMMA = new Object();
	private static final int FIRST = 0;
	private static final int CURRENT = 1;
	private static final int NEXT = 2;

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

	private final Class<?> clazz;
	
	private CharacterIterator it;
	private char c;
	private Object token;
	private StringBuilder buf = new StringBuilder();

	public JsonReader(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public JsonReader() {
		this(null);
	}
	
	public void reset() {
		it = null;
		c = 0;
		token = null;
		buf.setLength(0);
	}

	protected char next() {
		c = it.next();
		return c;
	}

	protected void skipWhiteSpace() {
		while (Character.isWhitespace(c)) {
			next();
		}
	}

	public Object read(CharacterIterator ci, int start) {
		reset();
		it = ci;
		switch (start) {
		case FIRST:
			c = it.first();
			break;
		case CURRENT:
			c = it.current();
			break;
		case NEXT:
			c = it.next();
			break;
		}
		return read(clazz);
	}

	public Object read(CharacterIterator it) {
		return read(it, NEXT);
	}

	public <T> T read(String string) {
		return (T) read(new StringCharacterIterator(string), FIRST);
	}
	
	protected Object read() {
		return read((Class<?>) null);
	}
	
	protected Object read(Class<?> clazz) {
		skipWhiteSpace();
		char ch = c;
		next();
		switch (ch) {
		case '"':
			token = string(clazz);
			break;
		case '[':
			token = array(clazz);
			break;
		case ']':
			token = ARRAY_END;
			break;
		case ',':
			token = COMMA;
			break;
		case '{':
			token = clazz != null ? object(clazz) : object();
			break;
		case '}':
			token = OBJECT_END;
			break;
		case ':':
			token = COLON;
			break;
		case 't':
			next();
			next();
			next(); // assumed r-u-e
			token = Boolean.TRUE;
			break;
		case 'f':
			next();
			next();
			next();
			next(); // assumed a-l-s-e
			token = Boolean.FALSE;
			break;
		case 'n':
			next();
			next();
			next(); // assumed u-l-l
			token = null;
			break;
		default:
			c = it.previous();
			if (Character.isDigit(c) || c == '-') {
				token = number(clazz);
			}
		}
		return token;
	}

	protected Object object() {
		Map<Object, Object> ret = new LinkedHashMap<>();
		Object key = read();
		while (token != OBJECT_END) {
			read(); // should be a colon
			if (token != OBJECT_END) {
				ret.put(key, read());
				if (read() == COMMA) {
					key = read();
				}
			}
		}
		return ret;
	}
	
	protected Object object(Class<?> clazz) {
		Object ret = CloneHelper.newInstance(clazz);
		String key = key();
		while (token != OBJECT_END) {
			PropertyInterface property = FlatProperties.getProperties(clazz).get(key);
			if (property == null) {
				throw new IllegalStateException("Property not found: " + key + " on " + clazz.getSimpleName());
			}
			Class<?> fieldClass = property.getClazz();
			if (fieldClass == List.class) {
				fieldClass = GenericUtils.getGenericClass(property.getType());
			}
			read(); // should be a colon
			if (token != OBJECT_END) {
				FlatProperties.set(ret, key.toString(), read(fieldClass));
				if (read() == COMMA) {
					key = key();
				}
			}
		}
		return ret;
	}

	private String key() {
		Object ret = read();
		if (ret == OBJECT_END) {
			return null;
		}
		String key = (String) ret;
		int pos = key.indexOf("_");
		while (pos > 0 && pos < key.length() - 1) {
			key = key.substring(0, pos) + key.substring(pos+1, pos+2).toUpperCase() + key.substring(pos+2, key.length());
			pos = key.indexOf("_");
		}
		if (Character.isDigit(key.charAt(0))) {
			key = "_" + key;
		}
		
		return key;
	}

	protected Object array(Class<?> clazz) {
		List<Object> ret = new ArrayList<Object>();
		Object value = read(clazz);
		while (token != ARRAY_END) {
			ret.add(value);
			if (read() == COMMA) {
				value = read(clazz);
			}
		}
		return ret;
	}
	
	protected Object number(Class<?> clazz) {
		int length = 0;
		boolean isFloatingPoint = false;
		buf.setLength(0);

		if (c == '-') {
			add();
		}
		length += addDigits();
		if (c == '.') {
			add();
			length += addDigits();
			isFloatingPoint = true;
		}
		if (c == 'e' || c == 'E') {
			add();
			if (c == '+' || c == '-') {
				add();
			}
			addDigits();
			isFloatingPoint = true;
		}

		String s = buf.toString();
		if (clazz == Integer.class) {
			return Integer.valueOf(s);
		} else if (clazz == Long.class) {
			return Long.valueOf(s);
		} else if (clazz == BigDecimal.class) {
			return new BigDecimal(s);
		} else if (clazz == Double.class) {
			return Double.valueOf(s);
		} 

		return isFloatingPoint ? (length < 17) ? (Object) Double.valueOf(s) : new BigDecimal(s) : (length < 19) ? (Object) Long.valueOf(s)
				: new BigInteger(s);
	}

	protected int addDigits() {
		int ret;
		for (ret = 0; Character.isDigit(c); ++ret) {
			add();
		}
		return ret;
	}

	protected Object string(Class<?> clazz) {
		buf.setLength(0);
		while (c != '"') {
			if (c == '\\') {
				next();
				if (c == 'u') {
					add(unicode());
				} else {
					Object value = escapes.get(Character.valueOf(c));
					if (value != null) {
						add(((Character) value).charValue());
					}
				}
			} else {
				add();
			}
		}
		next();

		String string =  buf.toString();
		if (clazz == LocalDateTime.class) {
			string = string.replace(" ", "T");
			return LocalDateTime.parse(string);
		} else {
			return string;
		}
	}

	protected void add(char cc) {
		buf.append(cc);
		next();
	}

	protected void add() {
		add(c);
	}

	protected char unicode() {
		int value = 0;
		for (int i = 0; i < 4; ++i) {
			switch (next()) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				value = (value << 4) + c - '0';
				break;
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
				value = (value << 4) + (c - 'a') + 10;
				break;
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
				value = (value << 4) + (c - 'A') + 10;
				break;
			}
		}
		return (char) value;
	}
}