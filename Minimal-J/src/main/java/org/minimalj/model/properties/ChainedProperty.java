package org.minimalj.model.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class ChainedProperty implements PropertyInterface {
	private PropertyInterface next;
	private Field field;

	public ChainedProperty(Class<?> clazz, Field field, PropertyInterface next) {
		this.field = field;
		this.next = next;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return next.getDeclaringClass();
	}

	@Override
	public Object getValue(Object object) {
		try {
			object = field.get(object);
			return next.getValue(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValue(Object object, Object value) {
		try {
			object = field.get(object);
			next.setValue(object, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return next.getName();
	}

	@Override
	public String getPath() {
		return field.getName() + "." + next.getPath();
	}

	@Override
	public Type getType() {
		return next.getType();
	}
	
	@Override
	public Class<?> getClazz() {
		return next.getClazz();
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return next.getAnnotation(annotationClass);
	}

	@Override
	public boolean isFinal() {
		return next.isFinal();
	}
}