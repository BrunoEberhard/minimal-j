package org.minimalj.transaction.predicate;

import java.io.Serializable;

public class SearchPredicate<T> extends PersistencePredicate<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String query;
	private final Object[] keys;
	
	public SearchPredicate(String query) {
		this(query, null);
	}
	
	public SearchPredicate(String query, Object[] keys) {
		this.keys = keys;
		this.query = query;
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
	
	@Override
	public boolean test(T t) {
		return true;
		// todo
	}
}
