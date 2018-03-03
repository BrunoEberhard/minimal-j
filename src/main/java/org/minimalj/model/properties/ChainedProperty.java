package org.minimalj.model.properties;

import java.lang.annotation.Annotation;

public class ChainedProperty implements PropertyInterface {
	private final PropertyInterface property1;
	private final PropertyInterface property2;

	public ChainedProperty(PropertyInterface property1, PropertyInterface property2) {
		this.property1 = property1;
		this.property2 = property2;
	}
	
	public boolean isAvailableFor(Object object) {
		if (property1 instanceof ChainedProperty) {
			if (!((ChainedProperty) property1).isAvailableFor(object)) {
				return false;
			}
		} 
		return property1.getValue(object) != null;
	}
	
	@Override
	public Class<?> getDeclaringClass() {
		return property2.getDeclaringClass();
	}

	@Override
	public Object getValue(Object object) {
		Object value1 = property1.getValue(object);
		if (value1 != null) {
			return property2.getValue(value1);
		} else {
			return null;
		}
	}

	@Override
	public void setValue(Object object, Object value) {
		Object value1 = property1.getValue(object);
		if (value1 != null) {
			property2.setValue(value1, value);
		}
	}

	@Override
	public String getName() {
		return property2.getName();
	}
	
	@Override
	public String getPath() {
		return property1.getPath() + "." + property2.getPath();
	}
	
	@Override
	public Class<?> getGenericClass() {
		return property2.getGenericClass();
	}
	
	@Override
	public Class<?> getClazz() {
		return property2.getClazz();
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return property2.getAnnotation(annotationClass);
	}

	@Override
	public boolean isFinal() {
		return property2.isFinal();
	}
}