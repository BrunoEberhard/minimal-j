package org.minimalj.model;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
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
		if (View.class.isAssignableFrom(clazz)) return true;
		if (field.getAnnotation(ViewReference.class) != null) return true;
		return false;
	}
	
	/**
	 * 
	 * @param property property to be checked
	 * @return true if property or class of property is marked as reference
	 * or the property clazz implements view or the type of a list property
	 * implements view interface.
	 */
	public static boolean isReference(PropertyInterface property) {
		if (property.getAnnotation(ViewReference.class) != null) return true;
		Class<?> clazz = property.getClazz();
		if (clazz == List.class) {
			clazz = GenericUtils.getGenericClass(property.getType());
		}
		return View.class.isAssignableFrom(clazz);
	}
	
	public static Class<?> getReferencedClass(PropertyInterface property) {
		Class<?> clazz = property.getClazz();
		if (clazz == List.class) {
			clazz = GenericUtils.getGenericClass(property.getType());
		}
		if (property.getAnnotation(ViewReference.class) != null) {
			return clazz;
		} else {
			clazz = getViewedClass(clazz);
			if (clazz != null) {
				return clazz;
			} else {
				throw new IllegalArgumentException(property.getPath());	
			}
		}
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
