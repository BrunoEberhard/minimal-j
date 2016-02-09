package org.minimalj.backend.sql;

public class ElementId {

	private final Object id;
	private final String parentClassName;
	private final String fieldPath;
	
	public ElementId(Object id, String parentClassName, String fieldPath) {
		this.id = id;
		this.parentClassName = parentClassName;
		this.fieldPath = fieldPath;
	}
	
	public Object getId() {
		return id;
	}
	
	public String getParentClassName() {
		return parentClassName;
	}
	
	public String getFieldPath() {
		return fieldPath;
	}
}
