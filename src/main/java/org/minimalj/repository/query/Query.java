package org.minimalj.repository.query;

import java.io.Serializable;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;

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
		Property property = Keys.getProperty(key);
		String path = property.getPath();
		return new Order(this, path, ascending);
	}

	public Criteria getCriteria() {
		Query query = this;
		if (query instanceof Limit) {
			query = ((Limit) query).getQuery();
		}
		while (query instanceof Order) {
			query = ((Order) query).getQuery();
		}
		return (Criteria) query;
	}
}