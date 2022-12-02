package org.minimalj.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import org.minimalj.backend.Backend;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Property;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public class ViewUtils {

	/**
	 * Creates a view to a complete object. Meaning all fields existing on view and
	 * the complete object are copied from the complete object to the view.
	 * 
	 * @param <T>            type of the view object
	 * @param completeObject the source
	 * @param viewObject     the filled view object
	 * @return the view object (same as input)
	 */
	public static <T> T view(Object completeObject, T viewObject) {
		if (completeObject == null) return null;
		
		Map<String, Property> propertiesOfCompleteObject = FlatProperties.getProperties(completeObject.getClass());
		Map<String, Property> properties = FlatProperties.getProperties(viewObject.getClass());

		for (Map.Entry<String, Property> entry : properties.entrySet()) {
			Property property = propertiesOfCompleteObject.get(entry.getKey());
			Object value = property != null ? property.getValue(completeObject) : readByGetMethod(completeObject, entry.getKey());
			Property propertyView = entry.getValue();
			if (value != null && !propertyView.getClazz().isAssignableFrom(value.getClass()) && View.class.isAssignableFrom(propertyView.getClazz())) {
				value = ViewUtils.view(value, CloneHelper.newInstance(propertyView.getClazz()));
			} 
			propertyView.setValue(viewObject, value);
		}
		return viewObject;
	}

	private static Object readByGetMethod(Object completeObject, String name) {
		try {
			Method method = completeObject.getClass().getMethod("get" + StringUtils.upperFirstChar(name));
			return method.invoke(completeObject);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Invalid view field: " + name + " for view on " + completeObject.getClass().getSimpleName());
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Resolves a view object to the real object. Of course this is only possible by
	 * asking the Backend to read the complete object. This method expects the view
	 * to have an id.
	 * 
	 * @param <T>        type of the complete object
	 * @param viewObject the view object
	 * @return the complete object (could be newer as the view object as the Backend
	 *         is asked)
	 */
	public static <T> T viewed(View<T> viewObject) {
		if (viewObject == null) return null;
		
		@SuppressWarnings("unchecked")
		Class<T> viewedClass = (Class<T>) getViewedClass(viewObject.getClass());
		
		Object id = IdUtils.getId(viewObject);
		if (id == null) {
			return null;
		}
		
		return Backend.read(viewedClass, id);
	}
	
	public static Class<?> getViewedClass(Class<?> clazz) {
		while (clazz != null) {
			for (Type type : clazz.getGenericInterfaces()) {
				if (type instanceof ParameterizedType) {
					ParameterizedType parameterizedType = (ParameterizedType) type;
					Type rawType = parameterizedType.getRawType();
					if (rawType.equals(View.class)) {
						return GenericUtils.getGenericClass(parameterizedType);
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	public static Class<?> resolve(Class<?> clazz) {
		if (View.class.isAssignableFrom(clazz)) {
			Class<?> viewedClass = getViewedClass(clazz);
			return viewedClass;
		} else {
			return clazz;
		}
	}
	
}
