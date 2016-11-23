package org.minimalj.metamodel.model;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.Enabled;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.Properties;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;

public class MjProperty {
	public static final MjProperty $ = Keys.of(MjProperty.class);
	
	public enum MjPropertyType {
		INLINE, LIST, ENUM_SET, REFERENCE,
		// primitives
		String, Integer, Long, Boolean, BigDecimal, LocalDate,
		LocalTime, LocalDateTime, ByteArray;
		
		public boolean isPrimitive() {
			return ordinal() > REFERENCE.ordinal();
		}
	}
	
	public Object id;

	public MjPropertyType propertyType;

	@Size(1024)
	public String name;
	public MjEntity type;

	public Integer size;
	public Boolean notEmpty;
	public Boolean searched;
	public String enabled;
	
	private MjModel model;
	
	public MjProperty() {
		//
	}
	
	public MjProperty(MjModel model, Field field) {
		this.model = model;
		
		name = field.getName();
		this.propertyType = propertyType(field);
		if (propertyType == MjPropertyType.LIST || propertyType == MjPropertyType.ENUM_SET) {
			this.type = model.getEntity(GenericUtils.getGenericClass(field));
		} else if (!FieldUtils.isAllowedPrimitive(field.getType())) {
			this.type = model.getEntity(field.getType());
		}
		notEmpty = field.getAnnotation(NotEmpty.class) != null;
		searched = field.getAnnotation(Searched.class) != null;
		Enabled enabled = field.getAnnotation(Enabled.class);
		this.enabled = enabled != null ? enabled.value() : null;
		size = AnnotationUtil.getSize(Properties.getProperty(field), AnnotationUtil.OPTIONAL);
		if (size == -1) {
			size = null;
		}
	}

	private MjPropertyType propertyType(Field field) {
		Class<?> fieldType = field.getType();
		if (fieldType == String.class) return MjPropertyType.String;
		else if (fieldType == Integer.class) return MjPropertyType.Integer;
		else if (fieldType == Long.class) return MjPropertyType.Long;
		else if (fieldType == Boolean.class) return MjPropertyType.Boolean;
		else if (fieldType == BigDecimal.class) return MjPropertyType.BigDecimal;
		else if (fieldType == LocalDate.class) return MjPropertyType.LocalDate;
		else if (fieldType == LocalTime.class) return MjPropertyType.LocalTime;
		else if (fieldType == LocalDateTime.class) return MjPropertyType.LocalDateTime;

		else if (fieldType == List.class) return MjPropertyType.LIST;
		else if (fieldType == Set.class) return MjPropertyType.ENUM_SET;
		else if (FieldUtils.isFinal(field)) return MjPropertyType.INLINE;
		else return MjPropertyType.REFERENCE;
	}
	
	public String getFormattedType() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "formattedType", String.class);
		if (propertyType == MjPropertyType.LIST) {
			return "List<" + type.name + ">";
		} else if (propertyType == MjPropertyType.ENUM_SET) {
			return "Set<" + type.name + ">";
		} else if (type != null) {
			return type.name;
		} else {
			return propertyType.name();
		}
	}
	
	public MjModel getModel() {
		return model;
	}
}
