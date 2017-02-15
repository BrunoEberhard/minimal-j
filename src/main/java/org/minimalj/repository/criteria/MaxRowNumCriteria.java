package org.minimalj.repository.criteria;

public class MaxRowNumCriteria extends FieldCriteria {
	private static final long serialVersionUID = 1L;
	
	public MaxRowNumCriteria(int maxRowNum) {
		super("row", FieldOperator.less, maxRowNum);
	}
	
	@Override
	public ChainableQuery and(ChainableQuery other) {
		throw new IllegalArgumentException("Not allowed on this criteria");
	}
	
	@Override
	public ChainableQuery or(ChainableQuery other) {
		throw new IllegalArgumentException("Not allowed on this criteria");
	}
}
