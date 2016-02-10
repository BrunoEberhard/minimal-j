package org.minimalj.backend.sql;

public class ElementId {

	private final Object id;
	private final String tableName;
	
	public ElementId(Object id, String tableName) {
		this.id = id;
		this.tableName = tableName;
	}
	
	public Object getId() {
		return id;
	}
	
	public String getTableName() {
		return tableName;
	}
}
