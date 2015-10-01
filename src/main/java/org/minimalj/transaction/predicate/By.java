package org.minimalj.transaction.predicate;

import org.minimalj.transaction.predicate.Criteria.Filter;

public class By {

	public static final Criteria ALL = new Criteria();
	
	public static SearchCriteria search(String query) {
		return new SearchCriteria(query);
	}
	
	public static SearchCriteria search(String query, Object[] keys) {
		return new SearchCriteria(query, keys);
	}

	public static FieldCriteria field(Object key, Object value) {
		return new FieldCriteria(key, value);
	}

	public static FieldCriteria field(Object key, FieldOperator operator, Object value) {
		return new FieldCriteria(key, operator, value);
	}

	public static Criteria all() {
		return ALL;
	}

	public static Criteria filter(Filter filter) {
		return filter.getCriteria();
	}
}
