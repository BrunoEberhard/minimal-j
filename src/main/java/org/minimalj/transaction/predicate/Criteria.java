package org.minimalj.transaction.predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Criteria<T> {

	// TODO: check for recursion?
	public Criteria<T> and(Criteria<T> other) {
		if (other != null) {
			if (other instanceof AndCriteria) {
				((AndCriteria<T>) other).getCriterias().add(0, this);
				return other;
			} else {
				return new AndCriteria<T>(this, other);
			}
		} else {
			return this;
		}
	}

	public Criteria<T> and(Filter<T> filter) {
		Criteria<T> other = filter.getCriteria();
		return and(other);
	}
	
	public Criteria<T> or(Criteria<T> other) {
		if (other != null) {
			if (other instanceof OrCriteria) {
				((OrCriteria<T>) other).getCriterias().add(0, this);
				return other;
			} else {
				return new OrCriteria<T>(this, other);
			}
		} else {
			return this;
		}
	}
	
	public int getLevel() {
		return 0;
	}
	
	public static abstract class CombinedCriteria<T> extends Criteria<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		private final List<Criteria<T>> criterias;

		public CombinedCriteria(Criteria<T> criteria1, Criteria<T> criteria2) {
			// don't use Arrays.asList as Array might change later
			criterias = new ArrayList<>();
			criterias.add(criteria1);
			criterias.add(criteria2);
		}
		
		public CombinedCriteria(List<Criteria<T>> criterias) {
			this.criterias = criterias;
		}

		public List<Criteria<T>> getCriterias() {
			return criterias;
		}
	}
	
	public static class OrCriteria<T> extends CombinedCriteria<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		public OrCriteria(Criteria<T> criteria1, Criteria<T> criteria2) {
			super(criteria1, criteria2);
		}

		public OrCriteria(List<Criteria<T>> criterias) {
			super(criterias);
		}

		@Override
		public int getLevel() {
			return -1;
		}
		
		@Override
		public Criteria<T> or(Criteria<T> other) {
			if (other != null && !getCriterias().contains(other)) {
				getCriterias().add(other);
			}
			return this;
		}
	}	
	
	public static class AndCriteria<T> extends CombinedCriteria<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		public AndCriteria(Criteria<T> criteria1, Criteria<T> criteria2) {
			super(criteria1, criteria2);
		}

		public AndCriteria(List<Criteria<T>> criterias) {
			super(criterias);
		}

		@Override
		public Criteria<T> and(Criteria<T> other) {
			if (other != null && !getCriterias().contains(other)) {
				getCriterias().add(other);
			}
			return this;
		}

		@Override
		public int getLevel() {
			return 1;
		}
	}
	
	public static interface Filter<T> {
		public Criteria<T> getCriteria();
	}
}
