package ch.openech.mj.model;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.openech.mj.util.IdUtils;

public class Reference<T> implements Serializable {

	private final Map<Object, Object> values;
	
	private long referencedId;
	
	public Reference(Object... keys) {
		if (keys.length > 0) {
			values = new HashMap<>(keys.length * 2);
			for (Object key : keys) {
				values.put(key, null);
			}
		} else {
			values = Collections.emptyMap();
		}
	}

	public long getReferencedId() {
		return referencedId;
	}
	
	public void setReferencedId(long referencedId) {
		this.referencedId = referencedId;
	}
	
	public boolean isEmpty() {
		return referencedId == 0;
	}
	
	public Object get(Object key) {
		if (!values.containsKey(key)) throw new IllegalArgumentException("Not declared: " + key);
		return values.get(key);
	}

	public void set(Object key, Object value) {
		if (!values.containsKey(key)) throw new IllegalArgumentException("Not declared: " + key);
		values.put(key, value);
	}

	public void set(Object value) {
		if (value != null) {
			referencedId = IdUtils.getId(value);
			for (Object key : values.keySet()) {
				PropertyInterface property = Keys.getProperty(key);
				Object v = property.getValue(value);
				values.put(key, v);
			}
		} else {
			referencedId = 0;
			for (Object key : values.keySet()) {
				values.put(key, null);
			}
		}
	}

	public Map<String, PropertyInterface> getProperties() {
		Map<String, PropertyInterface> properties = new HashMap<>();
		for (Object key : values.keySet()) {
			PropertyInterface property = new ReferenceProperty(key);
			properties.put(property.getFieldName(), property);
		}
		return properties;
	}
	
	public static class ReferenceProperty implements PropertyInterface {
		private final Object key;
		private final PropertyInterface property;
		
		public ReferenceProperty(Object key) {
			this.key = key;
			this.property = Keys.getProperty(key);
		}

		public Class<?> getDeclaringClass() {
			return property.getDeclaringClass();
		}

		public String getFieldName() {
			return "^" + property.getFieldName();
		}

		public String getFieldPath() {
			return "^" + property.getFieldPath();
		}

		public Class<?> getFieldClazz() {
			return property.getFieldClazz();
		}

		public Type getType() {
			return property.getType();
		}

		public <S extends Annotation> S getAnnotation(Class<S> annotationClass) {
			return property.getAnnotation(annotationClass);
		}

		public Object getValue(Object object) {
			return ((Reference<?>) object).get(key);
		}

		public void setValue(Object object, Object value) {
			((Reference<?>) object).set(key, value);
		}

		public boolean isFinal() {
			return false;
		}
	}

}
