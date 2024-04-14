package org.minimalj.model.properties;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChainedProperty implements Property {
	private final Property property1;
	private final Property property2;

	public ChainedProperty(Property property1, Property property2) {
		this.property1 = property1;
		this.property2 = property2;
	}

	public List<Property> getChain() {
		List<Property> chain = new ArrayList<>();
		if (property1 instanceof ChainedProperty) {
			chain.addAll(((ChainedProperty) property1).getChain());
		} else {
			chain.add(property1);
		}
		if (property2 instanceof ChainedProperty) {
			chain.addAll(((ChainedProperty) property2).getChain());
		} else {
			chain.add(property2);
		}
		return chain;
	}

	public static List<Property> getChain(Property property) {
		return property instanceof ChainedProperty ? ((ChainedProperty) property).getChain() : Collections.singletonList(property);
	}

	public static Property buildChain(List<Property> chain) {
		if (chain.size() == 1) {
			return chain.get(0);
		} else {
			return new ChainedProperty(chain.get(0), buildChain(chain.subList(1, chain.size())));
		}
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
		} else {
			throw new NullPointerException(property1.getName() + " on " + property1.getDeclaringClass().getSimpleName() + " is null");
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

	@Override
	public String toString() {
		return property1.toString() + "." + property2.toString();
	}

}
