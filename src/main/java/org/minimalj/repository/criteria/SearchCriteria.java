package org.minimalj.repository.criteria;

import java.io.Serializable;

public class SearchCriteria extends Criteria implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String query;
	private final Object[] keys;
	private final boolean notEqual;
	
	public SearchCriteria(String query) {
		this(query, null);
	}

	public SearchCriteria(String query, Object[] keys) {
		this(query, keys, false);
	}
	
	public SearchCriteria(String query, Object[] keys, boolean notEqual) {
		this.keys = keys;
		this.query = query;
		this.notEqual = notEqual;
	}
	
	@Override
	public int getLevel() {
		return 0;
	}

	public Object[] getKeys() {
		return keys;
	}

	public String getQuery() {
		return query;
	}
	
	public boolean isNotEqual() {
		return notEqual;
	}
	
	public Criteria negate() {
		return new SearchCriteria(query, keys, !notEqual);
	}
}
