package org.minimalj.repository.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class Criteria extends Query implements Predicate<Object> {
	private static final long serialVersionUID = 1L;

	// TODO: check for recursion?
	public Criteria and(Criteria other) {
		if (other != null) {
			if (other instanceof AndCriteria) {
				((AndCriteria) other).getCriterias().add(0, this);
				return other;
			} else {
				return new AndCriteria(this, other);
			}
		} else {
			return this;
		}
	}

	public Criteria or(Criteria other) {
		if (other != null) {
			if (other instanceof OrCriteria) {
				((OrCriteria) other).getCriterias().add(0, this);
				return other;
			} else {
				return new OrCriteria(this, other);
			}
		} else {
			return this;
		}
	}
	
	public static abstract class CompoundCriteria extends Criteria implements Serializable {
		private static final long serialVersionUID = 1L;

		private final List<Criteria> criterias;

		public CompoundCriteria(Criteria criteria1, Criteria criteria2) {
			// don't use Arrays.asList as Array might change later
			criterias = new ArrayList<>();
			criterias.add(criteria1);
			criterias.add(criteria2);
		}
		
		public CompoundCriteria(List<Criteria> criterias) {
			this.criterias = criterias;
		}

		public List<Criteria> getCriterias() {
			return criterias;
		}
	}
	
	public static class OrCriteria extends CompoundCriteria implements Serializable {
		private static final long serialVersionUID = 1L;

		public OrCriteria(Criteria criteria1, Criteria criteria2) {
			super(criteria1, criteria2);
		}

		public OrCriteria(List<Criteria> criterias) {
			super(criterias);
		}

		@Override
		public Criteria or(Criteria other) {
			if (other != null && !getCriterias().contains(other)) {
				getCriterias().add(other);
			}
			return this;
		}
		
		@Override
		public boolean test(Object object) {
			return getCriterias().stream().anyMatch(c -> c.test(object));
		}
	}	
	
	public static class AndCriteria extends CompoundCriteria implements Serializable {
		private static final long serialVersionUID = 1L;

		public AndCriteria(Criteria criteria1, Criteria criteria2) {
			super(criteria1, criteria2);
		}

		public AndCriteria(List<Criteria> criterias) {
			super(criterias);
		}

		@Override
		public Criteria and(Criteria other) {
			if (other != null && !getCriterias().contains(other)) {
				getCriterias().add(other);
			}
			return this;
		}
		
		@Override
		public boolean test(Object object) {
			return getCriterias().stream().allMatch(c -> c.test(object));
		}
	}
	
}
