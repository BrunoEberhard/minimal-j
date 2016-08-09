package org.minimalj.example.newsfeed.xml;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;

/**
 * This is mostly a copy of open - ech code. I like the StaxParse. But is not
 * really wide known so I don't want to include these Utils in minimal-j.
 *
 */
public class StaxUtil {
	private static Logger logger = Logger.getLogger(StaxUtil.class.getName());
	private static final int MAX_DATE_LENGHT = "01-02-1934".length();
	private static final int MAX_DATE_TIME_LENGHT = "01-02-1934 12:12:12".length();
	
	public static LocalDate date(XMLEventReader xml) throws XMLStreamException {
		String text = token(xml);
		if (text.length() > MAX_DATE_LENGHT) {
			// Einige Schlaumeier hängen ein "+01:00" oder eine Zeitzone an
			text = text.substring(0, MAX_DATE_LENGHT);
		}
		return LocalDate.from(DateTimeFormatter.ISO_DATE.parse(text));
	}
	
	public static LocalDateTime dateTime(XMLEventReader xml) throws XMLStreamException {
		String text = token(xml);
		if (Character.isAlphabetic(text.charAt(0))) {
			return LocalDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(text));
		}
		if (text.length() > MAX_DATE_TIME_LENGHT) {
			// Einige Schlaumeier hängen ein "+01:00" oder eine Zeitzone an
			text = text.substring(0, MAX_DATE_TIME_LENGHT);
		}
		return LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(text));
	}

	public static String partial(XMLEventReader xml) throws XMLStreamException {
		String text = token(xml);
		// handle dates like 1950-08-30+01:00
		if (text != null && text.length() > 10) text = text.substring(0, 10);
		return text;
	}

	public static void simpleValue(XMLEventReader xml, Object object, Object key) throws XMLStreamException {
		PropertyInterface property = FlatProperties.getProperties(object.getClass()).get(key);
		if (property == null) {
			throw new IllegalArgumentException("Unknown field: " + key);
		}
		Object value = null;
		if (property.getClazz() == String.class) {
			value = token(xml);
		} else if (property.getClazz() == Boolean.class) {
			value = bulean(xml);
		} else if (property.getClazz() == Integer.class) {
			value = integer(xml);
		} else if (property.getClazz() == LocalDate.class) {
			value = date(xml);
		} else if (property.getClazz() == LocalDateTime.class) {
			value = dateTime(xml);			
		} else if (Enum.class.isAssignableFrom(property.getClazz())) {
			enuum(xml, object, property);
			return;
		} else {
			throw new IllegalArgumentException("Unknown field type: " + property.getClazz().getName());
		}
		property.setValue(object, value);
	}
	
	public static String token(XMLEventReader xml) throws XMLStreamException {
		String token = null;
		while (true) {
			XMLEvent event = xml.nextEvent();
			if (event.isCharacters()) {
				token = event.asCharacters().getData().trim();
			} else if (event.isEndElement()) {
				return token;
			} // else skip
		}
	}
	
	public static int integer(XMLEventReader xml) throws XMLStreamException {
		int i = 0;
		while (true) {
			XMLEvent event = xml.nextEvent();
			if (event.isCharacters()) {
				String token = event.asCharacters().getData().trim();
				i = Integer.parseInt(token);
			} else if (event.isEndElement()) {
				return i;
			} // else skip
		}
	}
	
	public static int bulean(XMLEventReader xml) throws XMLStreamException {
		int i = -1;
		while (true) {
			XMLEvent event = xml.nextEvent();
			if (event.isCharacters()) {
				String token = event.asCharacters().getData().trim();
				i = Boolean.parseBoolean(token) || "1".equals(token) ? 1 : 0;
			} else if (event.isEndElement()) {
				if (i == -1) throw new IllegalArgumentException();
				return i;
			} // else skip
		}
	}

	public static void enuum(XMLEventReader xml, Object object, Object key) throws XMLStreamException {
		PropertyInterface property = Keys.getProperty(key);
		enuum(xml, object, property);
	}
	
	public static void enuum(XMLEventReader xml, Object object, PropertyInterface property) throws XMLStreamException {
		boolean found = false;
		while (true) {
			XMLEvent event = xml.nextEvent();
			if (event.isCharacters()) {
				String token = event.asCharacters().getData().trim();
				enuum(token, object, property);
				found = true;
			} else if (event.isEndElement()) {
				if (!found) throw new IllegalArgumentException(); else return;
			} // else skip
		}
	}
	
	// in ech EchCode!!
	public static <T extends Enum<T>> void enuum(String value, Object object, PropertyInterface property) {
		@SuppressWarnings("unchecked")
		Class<T> enumClass = (Class<T>) property.getClazz();
		List<Enum> values = (List<Enum>) EnumUtils.valueList(enumClass);
		for (Enum enumValue : values) {
			if (enumValue.name().equalsIgnoreCase(value)) {
				property.setValue(object, enumValue);
				return;
			}
		}
		Object createdEnum = InvalidValues.createInvalidEnum(enumClass, value);
		property.setValue(object, createdEnum);
	}

	// in ech EchCode!!
	public static <T extends Enum<T>> T enuum(Class<T> enumClass, String value) {
		List<Enum> values = (List<Enum>) EnumUtils.valueList(enumClass);
		for (Enum enumValue : values) {
			if (enumValue.name().equalsIgnoreCase(value)) {
				return (T) enumValue;
			}
		}
		return InvalidValues.createInvalidEnum(enumClass, value);
	}

	public static void skip(XMLEventReader xml) throws XMLStreamException {
		while (true) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				logger.fine("Skipping XML Element: " + event.asStartElement().getName().getLocalPart());
				skip(xml);
			} else if (event.isEndElement()) break;
			// else ignore
		}
	}
	
}
