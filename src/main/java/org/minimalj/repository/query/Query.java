package org.minimalj.repository.query;

import java.io.Serializable;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

public abstract class Query implements Serializable {
	private static final long serialVersionUID = 1L;

	public Query limit(int rows) {
		return new Limit(this, rows);
	}

	public Query limit(Integer offset, int rows) {
		return new Limit(this, offset, rows);
	}

	public Order order(Object key) {
		return order(key, true);
	}

	public Order order(Object key, boolean ascending) {
		PropertyInterface property = Keys.getProperty(key);
		String path = property.getPath();
		return new Order(this, path, ascending);
	}

}