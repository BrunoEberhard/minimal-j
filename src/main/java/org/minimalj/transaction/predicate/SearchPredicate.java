package org.minimalj.transaction.predicate;

import java.io.Serializable;
import java.util.function.Predicate;

public class SearchPredicate<T> implements Predicate<T>, Serializable {
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
	
	@Override
	public Predicate<T> and(Predicate<? super T> other) {
		return new AndPredicate<T>(this, other);
	}

}
