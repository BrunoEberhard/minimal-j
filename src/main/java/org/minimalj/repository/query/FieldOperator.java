package org.minimalj.repository.query;

public enum FieldOperator {
	less("<"), greaterOrEqual(">="), lessOrEqual("<="), greater(">"), equal("="), notEqual("<>");
	
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
	
	public FieldOperator negate() {
		return FieldOperator.values()[this.ordinal() ^ 1];
	}
}