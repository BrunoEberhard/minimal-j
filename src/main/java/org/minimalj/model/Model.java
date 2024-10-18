package org.minimalj.model;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;

public interface Model {

	public default String getName() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Defines the (root) entities. These are the classes the are used for a
	 * repository. View classes should not be listed here.
	 * 
	 * @return all the classes used for this Model. These classes will be checked
	 *         for compliance by the ModelTest .
	 */
	public Class<?>[] getEntityClasses();

	/**
	 * Find all main entities (not the ones without id)
	 * 
	 * @param baseClasses the classes from the model
	 * @return the main entities 
	 */
	public static List<Class<?>> getEntityClassesRecursive(Class<?>[] baseClasses) {
		return getClassesRecursive(baseClasses, false, true);
	}
	
	public static List<Class<?>> getClassesRecursive(Class<?>[] baseClasses, boolean depthFirst, boolean onlyWithId) {
		List<Class<?>> classes = new ArrayList<>();
		List<Class<?>> visited = new ArrayList<>();
		for (Class<?> clazz : baseClasses) {
			getClassesRecursive(classes, visited, clazz, depthFirst, onlyWithId);
		}
		return classes;
	}
		
	static void getClassesRecursive(List<Class<?>> classes, List<Class<?>> visited, Class<?> clazz, boolean depthFirst, boolean onlyWithId) {
		if (!visited.contains(clazz)) {
			visited.add(clazz);
			if (!depthFirst) {
				classes.add(clazz);
			}
			for (Property property : Properties.getProperties(clazz).values()) {
				Class<?> propertyClass = property.getClazz();
				if (Collection.class.isAssignableFrom(propertyClass)  || propertyClass == Selection.class) {
					propertyClass = property.getGenericClass();
				}
				if (propertyClass == null) {
					continue;
				}
				if (View.class.isAssignableFrom(propertyClass)) {
					propertyClass = ViewUtils.getViewedClass(propertyClass);
				}
				if (Modifier.isAbstract(propertyClass.getModifiers())) {
					continue;
				}
				if (onlyWithId && IdUtils.hasId(propertyClass) || !onlyWithId && !FieldUtils.isAllowedPrimitive(propertyClass)) {
					getClassesRecursive(classes, visited, propertyClass, depthFirst, onlyWithId);
				}
			}
			if (depthFirst) {
				classes.add(clazz);
			}
		}
	}

}
