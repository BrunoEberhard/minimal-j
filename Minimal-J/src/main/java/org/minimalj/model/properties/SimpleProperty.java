package org.minimalj.model.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.minimalj.util.FieldUtils;

public class SimpleProperty implements PropertyInterface {
	private Field field;

	public SimpleProperty(Class<?> clazz, Field field) {
		this.field = field;
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
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValue(Object object, Object value) {
		try {
			field.set(object, value);
		} catch (Exception e) {
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
	public Type getType() {
		return field.getGenericType();
	}

	@Override
	public Class<?> getClazz() {
		return field.getType();
	}
	
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return field.getAnnotation(annotationClass);
	}

	@Override
	public boolean isFinal() {
		return FieldUtils.isFinal(field);
	}
}