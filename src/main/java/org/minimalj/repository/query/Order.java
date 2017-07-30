package org.minimalj.repository.query;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.query.Query.QueryLimitable;
import org.minimalj.repository.query.Query.QueryOrderable;

public class Order implements QueryLimitable, QueryOrderable {
	private static final long serialVersionUID = 1L;

	private final QueryLimitable query;
	private final String path;
	private final boolean ascending;
	
	public Order(QueryLimitable query, String path, boolean ascending) {
		this.query = query;
		this.path = path;
		this.ascending = ascending;
	}
	
	public QueryLimitable getQuery() {
		return query;
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	@Override
	public Order order(Object key, boolean ascending) {
		// the most significant order key should be the first one
		// (with super.order it would be at the end of the chain)
		
		PropertyInterface property = Keys.getProperty(key);
		String path = property.getPath();
		
		Order subOrder = new Order(this.query, path, ascending);
		return new Order(subOrder, this.path, this.ascending);
	}
}