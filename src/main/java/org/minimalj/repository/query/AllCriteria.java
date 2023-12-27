package org.minimalj.repository.query;

public class AllCriteria extends Criteria {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean test(Object t) {
		return true;
	}
	
	@Override
	public String toString() {
		return "all";
	}
}
