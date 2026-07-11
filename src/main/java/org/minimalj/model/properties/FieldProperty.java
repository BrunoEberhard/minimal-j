package org.minimalj.model.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.model.View;
import org.minimalj.model.ViewUtils;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.Property.StringBasedProperty;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.LoggingRuntimeException;

public class FieldProperty implements StringBasedProperty {
	private static Logger logger = Logger.getLogger(FieldProperty.class.getName());

	private final Field field;
	private final Class<?> declaringClass;
	private final boolean isFinal, isTransient;
	private final Class<?> type;
	private final boolean primitive;
	
	public FieldProperty(Field field, Class<?> declaringClass) {
		this.field = field;
		this.isFinal = FieldUtils.isFinal(field);
		this.isTransient = FieldUtils.isTransient(field);
		this.type = convertPrimitiveTypes(field.getType());
		this.declaringClass = declaringClass;
		this.primitive = field.getType().isPrimitive();
	}

	private static Class<?> convertPrimitiveTypes(Class<?> clazz) {
		if (clazz == Boolean.TYPE) {
			return Boolean.class;
		} else if (clazz == Integer.TYPE) {
			return Integer.class;
		} else if (clazz == Long.TYPE) {
			return Long.class;
		} else {
			return clazz;
		}
	}

	@Override
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public Object getValue(Object object) {
		try {
			return field.get(object);
		} catch (Exception e) {
			throw new LoggingRuntimeException(e, logger, "get of " + field.getName() + " failed");
		}
	}
	
	@Override
	public String getInvalidString(Object object) {
		return InvalidValues.getInvalidString(object, field);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setValue(Object object, Object value) {
		try {
			if (!isFinal) {
				if (value == null && primitive) {
					// a primitive field cannot hold null, fall back to its empty value
					if (type == Boolean.class) {
						value = Boolean.FALSE;
					} else if (type == Integer.class) {
						value = Integer.valueOf(0);
					} else {
						value = Long.valueOf(0L);
					}
				}
				field.set(object, value);
			} else {
				Object finalObject = field.get(object);
				if (finalObject == value)
					return;
				if (finalObject instanceof Collection) {
					Collection finalCollection = (Collection) finalObject;
					finalCollection.clear();
					if (value != null) {
						finalCollection.addAll((Collection) value);
					}
				} else {
					if (value == null) {
						value = EmptyObjects.getEmptyObject(finalObject.getClass());
					}
					CloneHelper.deepCopy(value, finalObject);
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setInvalidString(Object object, String string) {
		InvalidValues.setInvalidString(object, field, string);
	}
	
	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public String getPath() {
		return getName();
	}

	@Override
	public Class<?> getGenericClass() {
		return GenericUtils.getGenericClass(declaringClass, field);
	}
	
	public Field getField() {
		return field;
	}

	@Override
	public Class<?> getClazz() {
		return type;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		if (!annotations.containsKey(annotationClass)) {
			annotations.put(annotationClass, _getAnnotation(annotationClass));
		}
		return (T) annotations.get(annotationClass);
	}
	
	@Override
	public boolean notEmpty() {
		return primitive || getAnnotation(NotEmpty.class) != null;
	}

	private <T extends Annotation> T _getAnnotation(Class<T> annotationClass) {
		T annotation = field.getAnnotation(annotationClass);
		if (annotation == null && View.class.isAssignableFrom(getDeclaringClass())) {
			Class<?> viewedClass = ViewUtils.getViewedClass(getDeclaringClass());
			Property propertyInterface = Properties.getProperty(viewedClass, getName());
			return propertyInterface != null ? propertyInterface.getAnnotation(annotationClass) : null;
		} else {
			return annotation;
		}
	}

	private Map<Class<?>, Annotation> annotations = new HashMap<>();

	@Override
	public boolean isFinal() {
		return isFinal;
	}
	
	public boolean isTransient() {
		return isTransient;
	}

	@Override
	public String toString() {
		return "[" + field.getDeclaringClass().getSimpleName() + "." + field.getName() + "]";
	}
}