package org.minimalj.repository.criteria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChainableQuery implements Query {
	private static final long serialVersionUID = 1L;

	// TODO: check for recursion?
	public ChainableQuery and(ChainableQuery other) {
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

	public ChainableQuery or(ChainableQuery other) {
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
	
	public Query maxRowNowNum(int maxRowNum) {
		return and(new MaxRowNumCriteria(maxRowNum));
	}
	
	public PagingCriteria paging(int pageSize, int pageNum) {
		return new PagingCriteria(this, pageSize, pageNum);
	}
	
	public int getLevel() {
		return 0;
	}
	
	public static abstract class CombinedCriteria extends ChainableQuery implements Serializable {
		private static final long serialVersionUID = 1L;

		private final List<ChainableQuery> criterias;

		public CombinedCriteria(ChainableQuery criteria1, ChainableQuery criteria2) {
			// don't use Arrays.asList as Array might change later
			criterias = new ArrayList<>();
			criterias.add(criteria1);
			criterias.add(criteria2);
		}
		
		public CombinedCriteria(List<ChainableQuery> criterias) {
			this.criterias = criterias;
		}

		public List<ChainableQuery> getCriterias() {
			return criterias;
		}
	}
	
	public static class OrCriteria extends CombinedCriteria implements Serializable {
		private static final long serialVersionUID = 1L;

		public OrCriteria(ChainableQuery criteria1, ChainableQuery criteria2) {
			super(criteria1, criteria2);
		}

		public OrCriteria(List<ChainableQuery> criterias) {
			super(criterias);
		}

		@Override
		public int getLevel() {
			return -1;
		}
		
		@Override
		public ChainableQuery or(ChainableQuery other) {
			if (other != null && !getCriterias().contains(other)) {
				getCriterias().add(other);
			}
			return this;
		}
	}	
	
	public static class AndCriteria extends CombinedCriteria implements Serializable {
		private static final long serialVersionUID = 1L;

		public AndCriteria(ChainableQuery criteria1, ChainableQuery criteria2) {
			super(criteria1, criteria2);
		}

		public AndCriteria(List<ChainableQuery> criterias) {
			super(criterias);
		}

		@Override
		public ChainableQuery and(ChainableQuery other) {
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
	
}
