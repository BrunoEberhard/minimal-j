package ch.openech.mj.db.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.openech.mj.edit.value.Reference;
import ch.openech.mj.util.FieldUtils;

// TODO zusammenhang mit ColumnAccess???
public class ListColumnAccess {
	private static final Map<Class<?>, Map<String, AccessorInterface>> accessors = 
		new HashMap<Class<?>, Map<String, AccessorInterface>>();

	//

	public static Map<String, AccessorInterface> getAccessors(Class<?> clazz) {
		if (!accessors.containsKey(clazz)) {
			accessors.put(clazz, accessors(clazz));
		}
		Map<String, AccessorInterface> accessorsForClass = accessors.get(clazz);
		return accessorsForClass;
	}
	
	private static Map<String, AccessorInterface> accessors(Class<?> clazz) {
		Map<String, AccessorInterface> accessors = new LinkedHashMap<String, AccessorInterface>();
		
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field)) continue;
			
			boolean isReference = field.getAnnotation(Reference.class) != null;
			if (!isReference && FieldUtils.isFinal(field) && !FieldUtils.isList(field)) {
				Map<String, AccessorInterface> inlineAccessors = accessors(field.getType());
				boolean hasClassName = ColumnAccessUtils.hasClassName(field);
				for (String inlineKey : inlineAccessors.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = field.getName();
						key += Character.toUpperCase(inlineKey.charAt(0));
						if (inlineKey.length() > 1) {
							key += inlineKey.substring(1);
						}
					}
					accessors.put(key, new ColumnAccess.ChainedAccessor(field, inlineAccessors.get(inlineKey)));
				}
			} else if (FieldUtils.isList(field)) {
				accessors.put(field.getName(), new ColumnAccess.ColumnAccessor(field));
			}
		}
		return accessors; 
	}
	
}
