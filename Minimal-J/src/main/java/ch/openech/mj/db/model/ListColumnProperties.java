package ch.openech.mj.db.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.annotation.Reference;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.StringUtils;

/**
 * Framework internal.<p>
 * 
 * Provides for each class a map of Properties. But only the
 * properties that are of the class List.
 *
 */
public class ListColumnProperties {
	private static final Map<Class<?>, Map<String, PropertyInterface>> properties = 
		new HashMap<Class<?>, Map<String, PropertyInterface>>();

	//

	public static Map<String, PropertyInterface> getProperties(Class<?> clazz) {
		if (!properties.containsKey(clazz)) {
			properties.put(clazz, properties(clazz));
		}
		Map<String, PropertyInterface> propertiesForClass = properties.get(clazz);
		return propertiesForClass;
	}
	
	private static Map<String, PropertyInterface> properties(Class<?> clazz) {
		Map<String, PropertyInterface> properties = new LinkedHashMap<String, PropertyInterface>();
		
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field)) continue;
			
			boolean isReference = field.getAnnotation(Reference.class) != null;
			if (!isReference && FieldUtils.isFinal(field) && !FieldUtils.isList(field)) {
				// This is needed to check if an inline Property contains a List
				Map<String, PropertyInterface> inlinePropertys = properties(field.getType());
				boolean hasClassName = ColumnPropertyUtils.hasClassName(field);
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = field.getName() + StringUtils.upperFirstChar(inlineKey);
					}
					properties.put(key, new ColumnProperties.ChainedProperty(clazz, field, inlinePropertys.get(inlineKey)));
				}
			} else if (FieldUtils.isList(field)) {
				properties.put(field.getName(), new ColumnProperties.ColumnProperty(clazz, field));
			}
		}
		return properties; 
	}
	
}
