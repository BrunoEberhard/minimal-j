package org.minimalj.persistence.criteria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Criteria implements Serializable {
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

	public Criteria and(Filter filter) {
		Criteria other = filter.getCriteria();
		return and(other);
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
	
	public int getLevel() {
		return 0;
	}
	
	public static abstract class CombinedCriteria extends Criteria implements Serializable {
		private static final long serialVersionUID = 1L;

		private final List<Criteria> criterias;

		public CombinedCriteria(Criteria criteria1, Criteria criteria2) {
			// don't use Arrays.asList as Array might change later
			criterias = new ArrayList<>();
			criterias.add(criteria1);
			criterias.add(criteria2);
		}
		
		public CombinedCriteria(List<Criteria> criterias) {
			this.criterias = criterias;
		}

		public List<Criteria> getCriterias() {
			return criterias;
		}
	}
	
	public static class OrCriteria extends CombinedCriteria implements Serializable {
		private static final long serialVersionUID = 1L;

		public OrCriteria(Criteria criteria1, Criteria criteria2) {
			super(criteria1, criteria2);
		}

		public OrCriteria(List<Criteria> criterias) {
			super(criterias);
		}

		@Override
		public int getLevel() {
			return -1;
		}
		
		@Override
		public Criteria or(Criteria other) {
			if (other != null && !getCriterias().contains(other)) {
				getCriterias().add(other);
			}
			return this;
		}
	}	
	
	public static class AndCriteria extends CombinedCriteria implements Serializable {
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
		public int getLevel() {
			return 1;
		}
	}
	
	public static interface Filter {
		public Criteria getCriteria();
	}
}
