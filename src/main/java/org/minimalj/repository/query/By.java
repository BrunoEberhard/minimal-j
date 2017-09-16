package org.minimalj.repository.query;

import org.minimalj.util.StringUtils;

public class By {

	public static final AllCriteria ALL = new AllCriteria();
	public static final boolean ADD_WILDCARDS = true;
	
	public static SearchCriteria search(String query) {
		return search(query, ADD_WILDCARDS);
	}
	
	public static SearchCriteria search(String query, boolean addWildcards) {
		if (!StringUtils.isEmpty(query)) {
			if (addWildcards && !query.contains("*")) {
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
	
	public static SearchCriteria search(String query, Object... keys) {
		return new SearchCriteria(query, keys);
	}

	public static FieldCriteria field(Object key, Object value) {
		return new FieldCriteria(key, value);
	}

	public static FieldCriteria field(Object key, FieldOperator operator, Object value) {
		return new FieldCriteria(key, operator, value);
	}

	public static AllCriteria all() {
		return ALL;
	}

	public static Limit limit(int rows) {
		return new Limit(ALL, rows);
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
