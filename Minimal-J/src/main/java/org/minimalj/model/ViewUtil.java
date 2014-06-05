package org.minimalj.model;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import org.minimalj.model.annotation.View;
import org.minimalj.model.annotation.ViewOf;
import org.minimalj.model.properties.FlatProperties;
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
	
	/**
	 * 
	 * @param property
	 * @return true if property or class of property is annotated as View
	 */
	public static boolean isView(Field field) {
		Class<?> clazz = field.getType();
		if (ViewOf.class.isAssignableFrom(clazz)) return true;
		if (field.getAnnotation(View.class) != null) return true;
		return false;
	}
	
	/**
	 * 
	 * @param property
	 * @return true if property or class of property is annotated as View
	 */
	public static boolean isView(PropertyInterface property) {
		Class<?> clazz = property.getFieldClazz();
		if (ViewOf.class.isAssignableFrom(clazz)) return true;
		if (property.getAnnotation(View.class) != null) return true;
		return false;
	}
	
	public static Class<?> getViewedClass(PropertyInterface property) {
		if (!isView(property)) throw new IllegalArgumentException(property.getFieldPath());
		View view = property.getAnnotation(View.class);
		if (view != null) return property.getFieldClazz();
		
		Class<?> clazz = property.getFieldClazz();
		return getViewedClass(clazz);
	}
	
	public static Class<?> getViewedClass(Class<?> clazz) {
		for (Type type : clazz.getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type rawType = parameterizedType.getRawType();
				if (rawType.equals(ViewOf.class)) {
					return GenericUtils.getGenericClass(parameterizedType);
				}
			}
		}
		return null;
	}

	public static Class<?> resolve(Class<?> clazz) {
		if (ViewOf.class.isAssignableFrom(clazz)) {
			Class<?> viewedClass = getViewedClass(clazz);
			return viewedClass;
		} else {
			return clazz;
		}

	}
	
}
