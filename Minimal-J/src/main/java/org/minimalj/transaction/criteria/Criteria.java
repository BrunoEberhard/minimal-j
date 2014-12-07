package org.minimalj.transaction.criteria;

import java.io.Serializable;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

public abstract class Criteria implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static Criteria equals(Object key, Object value) {
		return new SimpleCriteria(key, CriteriaOperator.equal, value);
	}

	public static SearchCriteria search(String searchText) {
		return new SearchCriteria(null, searchText);
	}
	
	public static SearchCriteria search(String searchText, Object... keys) {
		return new SearchCriteria(keys, searchText);
	}

	public static AllCriteria all() {
		return new AllCriteria();
	}

	public static class SimpleCriteria extends Criteria {
		private static final long serialVersionUID = 1L;

		private final CriteriaOperator operator;
		private final Object key;
		private final Object value;
		
		public SimpleCriteria(Object key, Object value) {
			this(key, CriteriaOperator.equal, value);
		}

		public SimpleCriteria(Object key, CriteriaOperator operator, Object value) {
			this.key = key;
			this.operator = operator;
			this.value = value;
			checkOperator(Keys.getProperty(key), operator);
		}

		private void checkOperator(PropertyInterface property, CriteriaOperator operator) {
			Class<?> clazz = property.getFieldClazz();
			if (clazz == Integer.class || clazz == Long.class) return;
			if (operator == CriteriaOperator.equal) return;
			throw new IllegalArgumentException(operator + " only allowed for integer and long fields");
		}

		public CriteriaOperator getOperator() {
			return operator;
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}
	}
	
	public static class SearchCriteria extends Criteria {
		private static final long serialVersionUID = 1L;

		private final Object[] keys;
		private final String query;

		public SearchCriteria(Object[] keys, String query) {
			this.keys = keys;
			this.query = query;
		}

		public Object[] getKeys() {
			return keys;
		}

		public String getQuery() {
			return query;
		}
	}
	
	public static class AllCriteria extends Criteria {
		private static final long serialVersionUID = 1L;

		// empty for all objects of a class
	}
	
}
