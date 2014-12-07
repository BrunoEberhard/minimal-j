package org.minimalj.model.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.LoggingRuntimeException;

public class FieldProperty implements PropertyInterface {
	private static Logger logger = Logger.getLogger(FieldProperty.class.getName());

	private final Field field;
	private final boolean isFinal;
	
	public FieldProperty(Field field) {
		this.field = field;
		this.isFinal = FieldUtils.isFinal(field);
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
				if (finalObject == value) return;
				if (finalObject instanceof List) {
					List finalList = (List) finalObject;
					finalList.clear();
					if (value != null) {
						finalList.addAll((List) value);
					}
				} else {
					if (value == null) {
						throw new IllegalArgumentException("Field " + field.getName() + " is final and cannot be set to null");
					}
					CloneHelper.deepCopy(value, finalObject);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getFieldName() {
		return field.getName();
	}

	@Override
	public String getFieldPath() {
		return getFieldName();
	}

	@Override
	public Type getType() {
		return field.getGenericType();
	}

	@Override
	public Class<?> getFieldClazz() {
		return field.getType();
	}
	
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return field.getAnnotation(annotationClass);
	}

	@Override
	public boolean isFinal() {
		return isFinal;
	}
}