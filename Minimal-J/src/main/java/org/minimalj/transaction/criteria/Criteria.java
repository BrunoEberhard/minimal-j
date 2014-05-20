package org.minimalj.transaction.criteria;

import org.minimalj.model.Keys;
import org.minimalj.model.PropertyInterface;
import org.minimalj.transaction.criteria.Criteria.JoinCriteria.JoinType;

public abstract class Criteria {
	
	public static Criteria equals(Object key, Object value) {
		return new SimpleCriteria(key, CriteriaOperator.equal, value);
	}

	public static Criteria and(Criteria criteria1, Criteria criteria2) {
		return new JoinCriteria(criteria1, JoinType.and, criteria2);
	}

	public static Criteria or(Criteria criteria1, Criteria criteria2) {
		return new JoinCriteria(criteria1, JoinType.or, criteria2);
	}

	public static class SimpleCriteria extends Criteria {
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
	
	public static class MaxResultsCriteria extends Criteria {
		private final int maxResults;
		
		public MaxResultsCriteria(int maxResults) {
			this.maxResults = maxResults;
		}
		
		public int getMaxResults() {
			return maxResults;
		}
	}

	public static class JoinCriteria extends Criteria {
		public static enum JoinType {and, or}
		
		private final Criteria criteria1, criteria2;
		private final JoinType joinType;
		
		public JoinCriteria(Criteria criteria1, JoinType joinType, Criteria criteria2) {
			this.criteria1 = criteria1;
			this.joinType = joinType;
			this.criteria2 = criteria2;
		}
	}

}
