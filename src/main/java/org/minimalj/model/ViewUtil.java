package org.minimalj.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.GenericUtils;

public class ViewUtil {

	public static <T> T view(Object completeObject, T viewObject) {
		if (completeObject == null) return null;
		
		Map<String, PropertyInterface> propertiesOfCompleteObject = FlatProperties.getProperties(completeObject.getClass());
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(viewObject.getClass());

		for (Map.Entry<String, PropertyInterface> entry : properties.entrySet()) {
			PropertyInterface property = propertiesOfCompleteObject.get(entry.getKey());
			Object value = property.getValue(completeObject);
			entry.getValue().setValue(viewObject, value);
		}
		return viewObject;
	}
	
	public static Class<?> getViewedClass(Class<?> clazz) {
		if (Code.class.isAssignableFrom(clazz)) {
			return clazz;
		}
		for (Type type : clazz.getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type rawType = parameterizedType.getRawType();
				if (rawType.equals(View.class)) {
					return GenericUtils.getGenericClass(parameterizedType);
				}
			}
		}
		return null;
	}

	public static Class<?> resolve(Class<?> clazz) {
		if (Code.class.isAssignableFrom(clazz)) {
			return clazz;
		} else if (View.class.isAssignableFrom(clazz)) {
			Class<?> viewedClass = getViewedClass(clazz);
			return viewedClass;
		} else {
			return clazz;
		}
	}
	
}
