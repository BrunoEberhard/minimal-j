package org.minimalj.transaction.criteria;

public enum CriteriaOperator {
	less("<"), lessOrEqual("<="), equal("="), greaterOrEqual(">="), greater(">");
	
	private final String operatorAsString;

	private CriteriaOperator(String operatorAsString) {
		this.operatorAsString = operatorAsString;
	}

	public String getOperatorAsString() {
		return operatorAsString;
	}
	
}