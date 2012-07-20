package ch.openech.mj.db.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import ch.openech.mj.edit.value.Reference;
import ch.openech.mj.edit.value.Required;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.StringUtils;

public class ColumnAccess {
	private static final Logger logger = Logger.getLogger(ColumnAccess.class.getName());
	
	private static final Map<Class<?>, Map<String, AccessorInterface>> accessors = 
		new HashMap<Class<?>, Map<String, AccessorInterface>>();

	public static Object getValue(Object domainObject, String key) {
		Class<?> clazz = domainObject.getClass();
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		AccessorInterface accessorInterface = accessorsForClass.get(key);
		if (accessorInterface != null) {
			return accessorInterface.getValue(domainObject);
		} else {
			logger.severe("No column " + key + " in Class " + clazz.getName());
			return null;
		}
	}
	
	public static Class<?> getType(Class<?> clazz, String key) {
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		AccessorInterface accessorInterface = accessorsForClass.get(key);
		if (accessorInterface != null) {
			return accessorInterface.getClazz();
		} else {
			logger.severe("No column " + key + " in Class " + clazz.getName());
			return null;
		}
	}

	public static Object getValueIgnoreCase(Object domainObject, String key) {
		Class<?> clazz = domainObject.getClass();
		AccessorInterface accessorInterface = getAccessorIgnoreCase(clazz, key);
		if (accessorInterface != null) {
			return accessorInterface.getValue(domainObject);
		} else {
			logger.severe("No column " + key + " in Class " + clazz.getName());
			return null;
		}
	}
	
	public static void setValue(Object domainObject, String key, Object value) {
		Class<?> clazz = domainObject.getClass();
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		accessorsForClass.get(key).setValue(domainObject, value);
	}
	
	public static void setValueIgnoreCase(Object domainObject, String key, Object value) {
		Class<?> clazz = domainObject.getClass();
		AccessorInterface accessorInterface = getAccessorIgnoreCase(clazz, key);
		if (accessorInterface != null) {
			accessorInterface.setValue(domainObject, value);
		}
	}

	public static AccessorInterface getAccessorIgnoreCase(Class<?> clazz, String key) {
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		AccessorInterface accessorInterface = null;
		for (Map.Entry<String, AccessorInterface> entry : accessorsForClass.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(key)) {
				accessorInterface = entry.getValue();
			}
		}
		return accessorInterface;
	}

	public static Set<String> getKeys(Class<?> clazz) {
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		return accessorsForClass.keySet();
	}
	
	public static List<String> getNonListKeys(Class<?> clazz) {
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		List<String> keys = new ArrayList<String>();
		for (Map.Entry<String, AccessorInterface> entry : accessorsForClass.entrySet()) {
			if (!FieldUtils.isList(entry.getValue().getClazz())) {
				keys.add(entry.getKey());
			}
		}
		return keys;
	}
	
	public static void copy(Object from, Object to) {
		Map<String, AccessorInterface> accessors = getAccessors(from.getClass());
		for (AccessorInterface accessor : accessors.values()) {
			Object fromValue = accessor.getValue(from);
			if (accessor.isFinal()) {
				Object toValue = accessor.getValue(to);
				copy(fromValue, toValue);
			} else {
				accessor.setValue(to, fromValue);
			}
		}
	}
	
	//

	public static Map<String, AccessorInterface> getAccessors(Class<?> clazz) {
		if (!accessors.containsKey(clazz)) {
			accessors.put(clazz, accessors(clazz));
		}
		Map<String, AccessorInterface> accessorsForClass = accessors.get(clazz);
		return accessorsForClass;
	}
	
	public static boolean isReference(AccessorInterface accessor) {
		if (accessor.getClazz().getName().startsWith("java.lang")) return false;
		if (accessor.getAnnotation(Reference.class) != null) return true;
		return !accessor.isFinal();
	}
	
	public static boolean isRequired(AccessorInterface accessor) {
		return accessor.getAnnotation(Required.class) != null;
	}
	
	private static Map<String, AccessorInterface> accessors(Class<?> clazz) {
		Map<String, AccessorInterface> accessors = new LinkedHashMap<String, AccessorInterface>();
		
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field)) continue;
			
			if (FieldUtils.isFinal(field) && !FieldUtils.isList(field)) {
				boolean isReference = field.getAnnotation(Reference.class) != null;
				if (!isReference) {
					Map<String, AccessorInterface> inlineAccessors = accessors(field.getType());
					boolean hasClassName = ColumnAccessUtils.hasClassName(field);
					for (String inlineKey : inlineAccessors.keySet()) {
						String key = inlineKey;
						if (!hasClassName) {
							key = field.getName() + StringUtils.upperFirstChar(inlineKey);
						}
						accessors.put(key, new ChainedAccessor(field, inlineAccessors.get(inlineKey)));
					}
				} else {
					accessors.put(field.getName(), new FinalReferenceAccessor(field));
				}
			} else {
				accessors.put(field.getName(), new ColumnAccessor(field));
			}
		}
		return accessors; 
	}

	static class ColumnAccessor implements AccessorInterface {
		private Field field;

		public ColumnAccessor(Field field) {
			this.field = field;
		}

		@Override
		public Object getValue(Object object) {
			try {
				return field.get(object);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setValue(Object object, Object value) {
			try {
				field.set(object, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getName() {
			return field.getName();
		}

		@Override
		public Type getType() {
			return field.getGenericType();
		}

		@Override
		public Class<?> getClazz() {
			return field.getType();
		}
		
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return field.getAnnotation(annotationClass);
		}

		@Override
		public boolean isFinal() {
			return FieldUtils.isFinal(field);
		}
	}

	static class FinalReferenceAccessor extends ColumnAccessor {

		public FinalReferenceAccessor(Field field) {
			super(field);
		}

		@Override
		public void setValue(Object object, Object value) {
			Object finalValue = getValue(object);
			copy(value, finalValue);
		}
	}

	
	static class ChainedAccessor implements AccessorInterface {
		private AccessorInterface next;
		private Field field;

		public ChainedAccessor(Field field, AccessorInterface next) {
			this.field = field;
			this.next = next;
		}

		@Override
		public Object getValue(Object object) {
			try {
				object = field.get(object);
				return next.getValue(object);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setValue(Object object, Object value) {
			try {
				object = field.get(object);
				next.setValue(object, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getName() {
			return next.getName();
		}

		@Override
		public Type getType() {
			return next.getType();
		}
		
		@Override
		public Class<?> getClazz() {
			return next.getClazz();
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return next.getAnnotation(annotationClass);
		}

		@Override
		public boolean isFinal() {
			return next.isFinal();
		}
	}
	
	public static <T> boolean equals(T o1, T o2) {
		Set<String> keys = ColumnAccess.getKeys(o1.getClass());
		for (String key : keys) {
			Object value1 = ColumnAccess.getValue(o1, key);
			Object value2 = ColumnAccess.getValue(o2, key);
			if (value1 == null && value2 != null || value1 != null && !value1.equals(value2)) return false;
		}
		return true;
	}
	
}
