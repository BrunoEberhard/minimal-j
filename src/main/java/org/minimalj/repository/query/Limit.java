package org.minimalj.repository.query;

public class Limit implements Query {
	private static final long serialVersionUID = 1L;

	private final QueryLimitable query;
	private final Integer offset;
	private final int rows;
	
	public Limit(QueryLimitable query, int rows) {
		this(query, null, rows);
	}
	
	public Limit(QueryLimitable query, Integer offset, int rows) {
		this.query = query;
		this.offset = offset;
		this.rows = rows;
	}

	public QueryLimitable getQuery() {
		return query;
	}
	
	public int getRows() {
		return rows;
	}
	
	public Integer getOffset() {
		return offset;
	}
}
