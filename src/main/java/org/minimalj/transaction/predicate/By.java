package org.minimalj.transaction.predicate;

public class By {

	public static <T> SearchCriteria<T> search(String query) {
		return new SearchCriteria<T>(query);
	}
	
	public static <T> SearchCriteria<T> search(String query, Object[] keys) {
		return new SearchCriteria<T>(query, keys);
	}

	public static <T> FieldCriteria<T> field(Object key, Object value) {
		return new FieldCriteria<>(key, value);
	}

	public static <T> FieldCriteria<T> field(Object key, FieldOperator operator, Object value) {
		return new FieldCriteria<>(key, operator, value);
	}

	public static <T> Criteria<T> all() {
		// the null criteria accepts by convention every object
		return null;
	}

	public static interface PersistenceFilter<T> {
		public Criteria<T> criteria();
	}
	
	public static <T> Criteria<T> filter(PersistenceFilter<T> filter) {
		return filter.criteria();
	}
}
