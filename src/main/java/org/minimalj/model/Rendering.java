package org.minimalj.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;

import org.minimalj.frontend.impl.util.HtmlString;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;

/**
 * <p>
 * normally render and renderDescription (= Tooltip) will return a String (or a
 * StringBuilder). The only allowed / supported formatting character in this
 * String is the newline ('\n').
 * </p>
 * 
 * <p>
 * If you would like to use some html tags you can return a HtmlString. Only
 * some basic html tags are allowed.
 * </p>
 *
 */
public interface Rendering {

	public CharSequence render();
	
	public default CharSequence renderDescription() {
		return null;
	}
	
	// helper methods (framework internal)

	public static String toString(CharSequence c) {
		if (c instanceof HtmlString) {
			return ((HtmlString) c).getString();
		} else if (c != null) {
			return c.toString();
		} else {
			return null;
		}
	}

	public static String toString(Object o) {
		CharSequence c = render(o);
		return toString(c);
	}

	public static String toString(Object o, PropertyInterface property) {
		CharSequence c = render(o, property);
		return toString(c);
	}
	
	public static CharSequence render(Object o) {
		return render(o, null);
	}
	
	public static CharSequence render(Object o, PropertyInterface property) {
		if (o instanceof Rendering) {
			return ((Rendering) o).render();
		} else if (o instanceof Collection) {
			Collection<?> collection = (Collection<?>) o;
			if (!collection.isEmpty()) {
				StringBuilder s = new StringBuilder();
				for (Object item : collection) {
					s.append(render(item)).append(", ");
				}
				return s.substring(0, s.length() - 2);
			} else {
				return "";
			}
		} else if (o instanceof Enum) {
			@SuppressWarnings("rawtypes")
			Enum enumElement = (Enum) o;
			return EnumUtils.getText(enumElement);
		} else if (o instanceof LocalDate) {
			return DateUtils.getDateTimeFormatter().format((TemporalAccessor) o); 
		} else if (o instanceof LocalTime) {
			return DateUtils.getTimeFormatter(property).format((LocalTime) o); 
		} else if (o instanceof LocalDateTime) {
			String date = DateUtils.getDateTimeFormatter().format((TemporalAccessor) o);
			String time = DateUtils.getTimeFormatter(property).format((TemporalAccessor) o);
			return date + " " + time; 
		} else if (InvalidValues.isInvalid(o)) {
			return InvalidValues.getInvalidValue(o);
		} else if (o != null) {
			return o.toString();
		} else {
			return "";
		}
	}

	public static String toDescriptionString(Object o) {
		CharSequence c = o instanceof Rendering ? ((Rendering) o).renderDescription() : null;
		return toString(c);
	}

}