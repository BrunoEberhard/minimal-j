package org.minimalj.repository.criteria;

public class PagingCriteria implements Query {

	private final ChainableQuery baseCriteria;
	private final int pageSize, pageNum;
	
	public PagingCriteria(ChainableQuery baseCriteria, int pageSize, int pageNum) {
		this.baseCriteria = baseCriteria;
		this.pageSize = pageSize;
		this.pageNum = pageNum;
	}

}
