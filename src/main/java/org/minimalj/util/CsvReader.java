package org.minimalj.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.minimalj.model.Code;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;

/**
 * This class doesn't replace a library like OpenCSV. It cannot be configured
 * (except for skipping comments) and it does accept only valid files as
 * specified in the rfc and encoded in UTF-8.
 * <p>
 * 
 * @see <a href=
 *      "http://tools.ietf.org/html/rfc4180">http://tools.ietf.org/html/rfc4180</a>
 * 
 */
public class CsvReader {

	private static final char SEPARATOR = ',';
	private static final char QUOTE_CHAR = '"';

	private final PushbackReader reader;
	private String commentStart = null;

	private final BiFunction<Class<?>, Object, Object> objectProvier;
	
	public CsvReader(InputStream is) {
		this(is, null);
	}

	public CsvReader(InputStream is, BiFunction<Class<?>, Object, Object> objectProvier) {
		reader = new PushbackReader(new InputStreamReader(is));
		this.objectProvier = objectProvier;
	}

	public <T> List<T> readValues(Class<T> clazz) {
		List<String> fields = readRecord();
		List<PropertyInterface> properties = new ArrayList<>(fields.size());
		for (String field : fields) {
			try {
				field = field.trim();
				if (StringUtils.isEmpty(field)) {
					throw new RuntimeException("csv - file contains empty header field");
				}
				properties.add(FlatProperties.getProperty(clazz, field));
			} catch (IllegalArgumentException x) {
				throw new RuntimeException("No field " + field + " in " + clazz.getSimpleName() + ". Please check csv - file for missing or invalid header line");
			}
		}
		List<T> objects = new ArrayList<>();
		List<String> values = readRecord();
		while (values != null) {
			T object = CloneHelper.newInstance(clazz);
			for (int i = 0; i < fields.size(); i++) {
				String stringValue = values.get(i).trim();
				PropertyInterface property = properties.get(i);
				Object value;
				Class<?> propertyClazz = property.getClazz();
				try {
					if (Code.class.isAssignableFrom(propertyClazz)) {
						value = objectProvier.apply(propertyClazz, stringValue);
					} else {
						value = FieldUtils.parse(stringValue, propertyClazz);
					}
					property.setValue(object, value);
				} catch (Exception x) {
					throw new RuntimeException("Unparseable value. Field: " + fields.get(i) + " / Value: " + stringValue, x);
				}
			}
			objects.add(object);
			values = readRecord();
		}
		return objects;
	}

	public List<String> readRecord() {
		try {
			return _readRecord();
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	// header = name *(COMMA name)
	// record = field *(COMMA field)
	private List<String> _readRecord() throws IOException {
		while (true) {
			int c = reader.read();
			if (c < 0) {
				return null;
			} else if (!Character.isWhitespace(c)) {
				if (commentStart != null && commentStart.indexOf(c) >= 0) {
					while (c != '\n') {
						c = reader.read();
						if (c < 0) {
							return null;
						}
					}
				} else {
					reader.unread(c);
					break;
				}
			}
		}

		List<String> values = new ArrayList<>();
		String value = readField();
		values.add(value);
		while (value != null) {
			int c = reader.read();
			if (c == SEPARATOR) {
				value = readField();
				values.add(value);
			} else if (c < 0 || c == '\n') {
				value = null;
			}
		}
		return values;
	}

	// field = (escaped / non-escaped)
	String readField() throws IOException {
		int c;
		while (true) {
			c = reader.read();
			if (c == -1) {
				return "";
			} else if (!Character.isWhitespace(c)) {
				reader.unread(c);
				break;
			} else if (c == '\n') {
				reader.unread(c);
				return "";
			}
		}
		if (c == QUOTE_CHAR) {
			return readEscaped();
		} else {
			return readNonEscaped();
		}
	}

	// escaped = DQUOTE *(TEXTDATA / COMMA / CR / LF / 2DQUOTE) DQUOTE
	String readEscaped() throws IOException {
		StringBuilder s = new StringBuilder();
		int quote = reader.read();
		if (quote != QUOTE_CHAR)
			throw new IllegalStateException();
		do {
			int c = reader.read();
			if (c == -1) {
				throw new IllegalStateException("Non closing quote");
			} else if (c == QUOTE_CHAR) {
				int c2 = reader.read();
				if (c2 == QUOTE_CHAR) {
					s.append((char) c);
				} else {
					reader.unread(c2);
					return s.toString();
				}
			} else {
				s.append((char) c);
			}
		} while (true);
	}

	// non-escaped = *TEXTDATA
	String readNonEscaped() throws IOException {
		StringBuilder s = new StringBuilder();
		do {
			int c = reader.read();
			if (c < 0) {
				return s.toString().trim();
			}
			if (c == SEPARATOR || c == '\n') {
				reader.unread(c);
				return s.toString().trim();
			} else {
				s.append((char) c);
			}
		} while (true);
	}

	public void setCommentStart(String commentStart) {
		if (commentStart != null && commentStart.indexOf('\n') >= 0) {
			throw new IllegalArgumentException();
		}
		this.commentStart = commentStart;
	}

}
