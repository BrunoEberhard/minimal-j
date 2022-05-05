package org.minimalj.metamodel.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.Enabled;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public class MjProperty {
	public static final MjProperty $ = Keys.of(MjProperty.class);
	
	public enum MjPropertyType {
		INLINE, LIST, ENUM_SET, DEPENDABLE, VALUE;
	}
	
	public Object id;

	public MjPropertyType propertyType;

	@Size(1024)
	public String name;
	public MjEntity type;

	public Integer size;
	public Boolean notEmpty;
	public Boolean searched;
	public Boolean materialized;
	public Boolean autoIncrement;
	public TechnicalFieldType technical;
	public String enabled;
	
	public MjProperty() {
		//
	}
	
	public MjProperty(MjModel model, Field field) {
		name = field.getName();
		this.propertyType = propertyType(field);
		if (propertyType == MjPropertyType.LIST || propertyType == MjPropertyType.ENUM_SET) {
			this.type = model.getOrCreateEntity(GenericUtils.getGenericClass(field));
		} else if (!FieldUtils.isAllowedPrimitive(field.getType())) {
			this.type = model.getOrCreateEntity(field.getType());
		} else {
			this.type = MjEntity.PRIMITIVES.get(field.getType());
		}
		notEmpty = field.getAnnotation(NotEmpty.class) != null;
		searched = field.getAnnotation(Searched.class) != null;
		Enabled enabled = field.getAnnotation(Enabled.class);
		this.enabled = enabled != null ? enabled.value() : null;
		size = AnnotationUtil.getSize(Properties.getProperty(field), AnnotationUtil.OPTIONAL);
		if (size == -1) {
			size = null;
		}
		TechnicalField technicalFieldAnnotation = field.getAnnotation(TechnicalField.class);
		this.technical = technicalFieldAnnotation != null ? technicalFieldAnnotation.value() : null;
	}
	
	public MjProperty(MjModel model, Method method) {
		name = StringUtils.lowerFirstChar(method.getName().substring(3));
		PropertyInterface property = new Keys.MethodProperty(method.getReturnType(), name, method, null);
		
		Class<?> returnType = method.getReturnType();
		this.propertyType = propertyType(returnType, false);
		if (propertyType == MjPropertyType.LIST || propertyType == MjPropertyType.ENUM_SET) {
			this.type = model.getOrCreateEntity(GenericUtils.getGenericClass(returnType));
		} else if (!FieldUtils.isAllowedPrimitive(returnType)) {
			this.type = model.getOrCreateEntity(returnType);
		} else {
			this.type = MjEntity.PRIMITIVES.get(returnType);
		}
		notEmpty = method.getAnnotation(NotEmpty.class) != null;
		searched = method.getAnnotation(Searched.class) != null;
		Enabled enabled = method.getAnnotation(Enabled.class);
		this.enabled = enabled != null ? enabled.value() : null;
		size = AnnotationUtil.getSize(property, AnnotationUtil.OPTIONAL);
		if (size == -1) {
			size = null;
		}
		this.materialized = true;
		this.technical = null;
	}
	
	private MjPropertyType propertyType(Field field) {
		Class<?> fieldType = field.getType();
		return propertyType(fieldType, FieldUtils.isFinal(field));
	}
	
	private MjPropertyType propertyType(Class<?> fieldType, boolean isFinal) {
		if (fieldType == List.class) return MjPropertyType.LIST;
		else if (fieldType == Set.class) return MjPropertyType.ENUM_SET;
		else if (isFinal) return MjPropertyType.INLINE;
		else if (!FieldUtils.isAllowedPrimitive(fieldType) && !IdUtils.hasId(fieldType))
			return MjPropertyType.DEPENDABLE;
		else return MjPropertyType.VALUE;
	}
	
	public String getFormattedType() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "formattedType");
		
		String className = type.getClassName();
		
		if (propertyType == MjPropertyType.LIST) {
			return "List<" + className + ">";
		} else if (propertyType == MjPropertyType.ENUM_SET) {
			return "Set<" + className + ">";
		} else {
			if (size == null || size == 0) {
				return className;
			} else {
				return className + " (" + size + ")";
			}
		}
	}

}
