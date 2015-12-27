package org.minimalj.model;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import org.minimalj.model.annotation.ViewReference;
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
	
	/**
	 * 
	 * @param field field to be checked
	 * @return true if property or class of property is annotated as View
	 */
	public static boolean isView(Field field) {
		Class<?> clazz = field.getType();
		if (ViewReference.class.isAssignableFrom(clazz)) return true;
		if (field.getAnnotation(ViewReference.class) != null) return true;
		return false;
	}
	
	/**
	 * 
	 * @param property property to be checked
	 * @return true if property or class of property is marked as reference
	 */
	public static boolean isReference(PropertyInterface property) {
		Class<?> clazz = property.getClazz();
		if (ViewReference.class.isAssignableFrom(clazz)) return true;
		if (property.getAnnotation(ViewReference.class) != null) return true;
		return false;
	}
	
	public static Class<?> getReferencedClass(PropertyInterface property) {
		if (!isReference(property)) throw new IllegalArgumentException(property.getPath());
		ViewReference view = property.getAnnotation(ViewReference.class);
		if (view != null) return property.getClazz();
		
		Class<?> clazz = property.getClazz();
		return getViewedClass(clazz);
	}
	
	public static Class<?> getViewedClass(Class<?> clazz) {
		if (Code.class.isAssignableFrom(clazz)) {
			return clazz;
		}
		for (Type type : clazz.getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type rawType = parameterizedType.getRawType();
				if (rawType.equals(ViewReference.class)) {
					return GenericUtils.getGenericClass(parameterizedType);
				}
			}
		}
		return null;
	}

	public static Class<?> resolve(Class<?> clazz) {
		if (Code.class.isAssignableFrom(clazz)) {
			return clazz;
		} else if (ViewReference.class.isAssignableFrom(clazz)) {
			Class<?> viewedClass = getViewedClass(clazz);
			return viewedClass;
		} else {
			return clazz;
		}
	}
	
}
