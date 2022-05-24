package org.minimalj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;

public interface Model {

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
		for (Class<?> clazz : baseClasses) {
			getClassesRecursive(classes, clazz, depthFirst, onlyWithId);
		}
		return classes;
	}

	private static void getClassesRecursive(List<Class<?>> classes, Class<?> clazz, boolean depthFirst, boolean onlyWithId) {
		if (!classes.contains(clazz)) {
			classes.add(clazz);
			for (PropertyInterface property : Properties.getProperties(clazz).values()) {
				Class<?> propertyClass = property.getClazz();
				if (Collection.class.isAssignableFrom(propertyClass)) {
					propertyClass = property.getGenericClass();
				}
				if (onlyWithId && IdUtils.hasId(propertyClass) || !onlyWithId && !FieldUtils.isAllowedPrimitive(propertyClass) && !property.isFinal()) {
					getClassesRecursive(classes, propertyClass, depthFirst, onlyWithId);
				}
			}
			if (depthFirst) {
				classes.remove(clazz);
				classes.add(clazz);
			}
		}
	}

}
