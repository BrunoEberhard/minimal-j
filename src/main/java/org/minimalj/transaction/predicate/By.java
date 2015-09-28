package org.minimalj.transaction.predicate;

import java.util.function.Predicate;

public class By {

	public static <T> Predicate<T> search(String query) {
		return new SearchPredicate<T>(query);
	}
	
	public static <T> Predicate<T> search(String query, Object[] keys) {
		return new SearchPredicate<T>(query, keys);
	}

	public static <T> Predicate<T> field(Object key, Object value) {
		return new FieldPredicate<>(key, value);
	}

	public static <T> Predicate<T> field(Object key, FieldOperator operator, Object value) {
		return new FieldPredicate<>(key, operator, value);
	}

	public static <T> Predicate<T> all() {
		// the null predicate accepts by convention every object
		return null;
	}

}
