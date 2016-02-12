package org.minimalj.backend.sql;

public class ElementId {

	private final Object id;
	private final String tableName;
	private final int position;
	
	public ElementId(Object id, String tableName, int position) {
		this.id = id;
		this.tableName = tableName;
		this.position = position;
	}
	
	public Object getId() {
		return id;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public int getPosition() {
		return position;
	}
}
