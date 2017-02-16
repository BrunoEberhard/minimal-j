package org.minimalj.repository.query;

import java.io.Serializable;

import org.minimalj.repository.query.Query.QueryLimitable;

public class Order implements QueryLimitable, Serializable {
	private static final long serialVersionUID = 1L;

	private final Query query;
	private final String path;
	private final boolean ascending;
	
	public Order(Query query, String path, boolean ascending) {
		this.query = query;
		this.path = path;
		this.ascending = ascending;
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean isAscending() {
		return ascending;
	}
}