package org.minimalj.model;

/**
 * An application can implement this interface to explicitly define its
 * Transactions. This can be useful for external interfaces. For internal
 * frontend/backend communication this is not needed.
 * 
 */
public interface Api extends Model {

	public Class<?>[] getTransactionClasses();

}
