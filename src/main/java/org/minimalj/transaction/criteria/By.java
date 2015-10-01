package org.minimalj.transaction.criteria;

import org.minimalj.transaction.criteria.Criteria.Filter;
import org.minimalj.util.StringUtils;

public class By {

	public static final Criteria ALL = new Criteria();
	public static final boolean ADD_WILDCARDS = true;
	
	public static SearchCriteria search(String query) {
		return search(query, ADD_WILDCARDS);
	}
	
	public static SearchCriteria search(String query, boolean addWildcards) {
		if (!StringUtils.isEmpty(query)) {
			if (addWildcards) {
				if (!query.startsWith("*")) {
					query = "*" + query;
				}
				if (!query.endsWith("*")) {
					query = query + "*";
				}
			}
			return new SearchCriteria(query);
		} else {
			return null;
		}
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

	public static Criteria range(Object key, Object minIncluding, Object maxIncluding) {
		Criteria c = null;
		if (minIncluding != null) {
			c = new FieldCriteria(key, FieldOperator.greaterOrEqual, minIncluding);
		}
		if (maxIncluding != null) {
			c = new FieldCriteria(key, FieldOperator.lessOrEqual, maxIncluding).and(c);
		}
		return c;
	}
}
