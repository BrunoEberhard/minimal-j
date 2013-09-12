package ch.openech.mj.model.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import ch.openech.mj.model.PropertyInterface;

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
	public String getFieldName() {
		return next.getFieldName();
	}

	@Override
	public String getFieldPath() {
		return field.getName() + "." + next.getFieldName();
	}

	@Override
	public Type getType() {
		return next.getType();
	}
	
	@Override
	public Class<?> getFieldClazz() {
		return next.getFieldClazz();
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