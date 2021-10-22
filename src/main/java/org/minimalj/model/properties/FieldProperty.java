package org.minimalj.model.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.LoggingRuntimeException;

public class FieldProperty implements PropertyInterface {
	private static Logger logger = Logger.getLogger(FieldProperty.class.getName());

	private final Field field;
	private final boolean isFinal;
	private final Class<?> type;

	public FieldProperty(Field field) {
		this.field = field;
		this.isFinal = FieldUtils.isFinal(field);
		this.type = field.getType();
	}

	@Override
	public Class<?> getDeclaringClass() {
		return field.getDeclaringClass();
	}

	@Override
	public Object getValue(Object object) {
		try {
			return field.get(object);
		} catch (Exception e) {
			throw new LoggingRuntimeException(e, logger, "get of " + field.getName() + " failed");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setValue(Object object, Object value) {
		try {
			if (!isFinal) {
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
	public String getName() {
		return field.getName();
	}

	@Override
	public String getPath() {
		return getName();
	}

	@Override
	public Class<?> getGenericClass() {
		return GenericUtils.getGenericClass(field);
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

	private <T extends Annotation> T _getAnnotation(Class<T> annotationClass) {
		T annotation = field.getAnnotation(annotationClass);
		if (annotation == null && View.class.isAssignableFrom(getDeclaringClass())) {
			Class<?> viewedClass = ViewUtil.getViewedClass(getDeclaringClass());
			PropertyInterface propertyInterface = Properties.getProperty(viewedClass, getName());
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
}