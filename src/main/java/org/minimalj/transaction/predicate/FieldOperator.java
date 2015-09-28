package org.minimalj.transaction.predicate;

public enum FieldOperator {
	less("<"), lessOrEqual("<="), equal("="), greaterOrEqual(">="), greater(">"), notEqual("<>");
	
	private final String operatorAsString;

	private FieldOperator(String operatorAsString) {
		this.operatorAsString = operatorAsString;
	}

	public String getOperatorAsString() {
		return operatorAsString;
	}
	
	public boolean includesEqual() {
		return this == equal || this == lessOrEqual || this == greaterOrEqual;
	}
	
}