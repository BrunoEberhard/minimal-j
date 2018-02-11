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
	public Boolean technical;
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
		this.technical = field.getAnnotation(TechnicalField.class) != null;
	}
	
	public MjProperty(MjModel model, Method method) {
		this.model = model;
		
		name = StringUtils.lowerFirstChar(method.getName().substring(3));
		PropertyInterface property = new Keys.MethodProperty(method.getReturnType(), name, method, null);
		
		Class<?> returnType = method.getReturnType();
		this.propertyType = propertyType(returnType, false);
		if (propertyType == MjPropertyType.LIST || propertyType == MjPropertyType.ENUM_SET) {
			this.type = model.getEntity(GenericUtils.getGenericClass(returnType));
		} else if (!FieldUtils.isAllowedPrimitive(returnType)) {
			this.type = model.getEntity(returnType);
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
		this.technical = true;
	}
	
	private MjPropertyType propertyType(Field field) {
		Class<?> fieldType = field.getType();
		return propertyType(fieldType, FieldUtils.isFinal(field));
	}
	
	private MjPropertyType propertyType(Class<?> fieldType, boolean isFinal) {
		if (fieldType == List.class) return MjPropertyType.LIST;
		else if (fieldType == Set.class) return MjPropertyType.ENUM_SET;
		else if (isFinal) return MjPropertyType.INLINE;
		else if (!IdUtils.hasId(fieldType)) return MjPropertyType.DEPENDABLE;
		else return MjPropertyType.VALUE;
	}
	
	public String getFormattedType() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "formattedType");
		if (propertyType == MjPropertyType.LIST) {
			return "List<" + type.name + ">";
		} else if (propertyType == MjPropertyType.ENUM_SET) {
			return "Set<" + type.name + ">";
		} else if (type != null) {
			return type.name;
		} else {
			if (size == null) {
				return propertyType.name();
			} else {
				return propertyType.name() + " (" + size + ")";
			}
		}
	}
	
	public MjModel getModel() {
		return model;
	}
}
