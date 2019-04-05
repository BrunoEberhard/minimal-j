package org.minimalj.repository.query;

public class Order extends Query {
	private static final long serialVersionUID = 1L;

	private final Query query;
	private final String path;
	private final boolean ascending;
	
	public Order(Query query, String path, boolean ascending) {
		this.query = query;
		this.path = path;
		this.ascending = ascending;
	}
	
	public Query getQuery() {
		return query;
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
}