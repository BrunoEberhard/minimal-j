package org.minimalj.transaction.predicate;

import java.io.Serializable;
import java.util.function.Predicate;

public class SearchPredicate<T> extends PersistencePredicate<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String query;
	private final Object[] keys;
	private final boolean notEqual;
	
	public SearchPredicate(String query) {
		this(query, null);
	}

	public SearchPredicate(String query, Object[] keys) {
		this(query, keys, false);
	}
	
	public SearchPredicate(String query, Object[] keys, boolean notEqual) {
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
	
	@Override
	public Predicate<T> negate() {
		return new SearchPredicate<>(query, keys, !notEqual);
	}
	
	@Override
	public boolean test(T t) {
		return true;
		// todo
	}
}
