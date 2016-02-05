package org.minimalj.backend.sql;

/**
 * Entity objects with a ViewOnlyId are not meant to be persisted.
 * If you call insert or remove for this entities you'll get an exception.<p>
 * 
 * To update such an entity (re)load it first!
 *
 */
public class ReadOnlyId {

	private final Object id;
	
	public ReadOnlyId(Object id) {
		this.id = id;
	}
	
	public Object getId() {
		return id;
	}
}
