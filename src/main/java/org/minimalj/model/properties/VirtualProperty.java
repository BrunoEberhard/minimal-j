package org.minimalj.model.properties;

import java.lang.annotation.Annotation;

/**
 * Base class for properties which don't belong to an existing class.
 * <p>
 * 
 * This can be helpful if you want to edit a single property with two form
 * fields.
 * <p>
 * 
 * This class could be removed by adding default methods to the Property. This
 * is not done on purpose as it would pollute the interface and suggest
 * 'virtual' as the default implementation of a property (which it is not).
 */
public abstract class VirtualProperty implements Property {

	@Override
	public Class<?> getDeclaringClass() {
		return null;
	}

	@Override
	public String getPath() {
		return getName();
	}

	@Override
	public Class<?> getGenericClass() {
		return null;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

}
