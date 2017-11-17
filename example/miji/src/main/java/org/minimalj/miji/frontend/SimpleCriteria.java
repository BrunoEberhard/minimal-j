package org.minimalj.miji.frontend;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.query.Criteria;

public class SimpleCriteria extends Criteria {
	private static final long serialVersionUID = 1L;

	private final String key;
	private final String value;
	
	public SimpleCriteria(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public SimpleCriteria(Object key, String value) {
		PropertyInterface property = Keys.getProperty(key);
		if (property == null) {
			throw new IllegalArgumentException("Key must be a field from a $ constant or a " + PropertyInterface.class.getSimpleName());
		}
		this.key = property.getPath();
		if (this.key.contains(".")) {
			throw new IllegalArgumentException("Chained properties not allowed");
		}
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
