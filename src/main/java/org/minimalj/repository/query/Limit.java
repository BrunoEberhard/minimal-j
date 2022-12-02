package org.minimalj.repository.query;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;

public class Limit extends Query {
	private static final long serialVersionUID = 1L;

	private final Query query;
	private final Integer offset;
	private final int rows;
	
	public Limit(Query query, int rows) {
		this(query, null, rows);
	}
	
	public Limit(Query query, Integer offset, int rows) {
		this.query = query;
		this.offset = offset;
		this.rows = rows;
	}

	public Query getQuery() {
		return query;
	}
	
	public int getRows() {
		return rows;
	}
	
	public Integer getOffset() {
		return offset;
	}

	//

	public Query limit(int rows) {
		return new Limit(query, rows);
	}

	public Query limit(Integer offset, int rows) {
		return new Limit(query, offset, rows);
	}

	public Order order(Object key) {
		return order(key, true);
	}

	public Order order(Object key, boolean ascending) {
		Property property = Keys.getProperty(key);
		String path = property.getPath();
		return new Order(query, path, ascending);
	}

}
