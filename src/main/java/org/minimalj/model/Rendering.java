package org.minimalj.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;

public interface Rendering {

	public static enum RenderType {
		PLAIN_TEXT, HMTL;
	}
	
	/*
	 * Note: If asked for HTML the answer can be a plain text. 
	 */
	public String render(RenderType renderType);
	
	public default String renderDescription(RenderType renderType) {
		return null;
	}
	
	public default RenderType getPreferredRenderType(RenderType firstType, RenderType... otherTypes) {
		return firstType;
	}

	// helper methods
	
	public static String render(Object o, RenderType renderType) {
		return render(o, renderType, null);
	}
	
	public static String render(Object o, RenderType renderType, PropertyInterface property) {
		if (o instanceof Rendering) {
			return ((Rendering) o).render(renderType);
		} else if (o instanceof Collection) {
			Collection<?> collection = (Collection<?>) o;
			if (!collection.isEmpty()) {
				StringBuilder s = new StringBuilder();
				for (Object item : collection) {
					s.append(render(item, renderType)).append(", ");
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

}