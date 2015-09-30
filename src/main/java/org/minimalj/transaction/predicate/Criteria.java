package org.minimalj.transaction.predicate;

import java.io.Serializable;

public abstract class Criteria<T> {

	public Criteria<T> and(Criteria<T> other) {
		if (other != null) {
			return new AndCriteria<T>(this, other);
		} else {
			return this;
		}
	}

	public Criteria<T> or(Criteria<T> other) {
		if (other != null) {
			return new OrCriteria<T>(this, other);
		} else {
			return this;
		}
	}
	
	public abstract int getLevel();
	
	public static abstract class CombinedCriteria<T> extends Criteria<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		private final Criteria<? super T> criteria1;
		private final Criteria<? super T> criteria2;
		
		public CombinedCriteria(Criteria<? super T> criteria1, Criteria<? super T> criteria2) {
			this.criteria1 = criteria1;
			this.criteria2 = criteria2;
		}

		public Criteria<? super T> getCriteria1() {
			return criteria1;
		}
		
		public Criteria<? super T> getCriteria2() {
			return criteria2;
		}
	}
	
	public static class OrCriteria<T> extends CombinedCriteria<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		public OrCriteria(Criteria<? super T> criteria1, Criteria<? super T> criteria2) {
			super(criteria1, criteria2);
		}

		@Override
		public int getLevel() {
			return -1;
		}
	}	
	
	public static class AndCriteria<T> extends CombinedCriteria<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		public AndCriteria(Criteria<? super T> criteria1, Criteria<? super T> criteria2) {
			super(criteria1, criteria2);
		}

		@Override
		public int getLevel() {
			return 1;
		}
	}	
}
