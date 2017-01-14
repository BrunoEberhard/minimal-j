package org.minimalj.transaction;

import java.io.Serializable;

/**
 * The transaction is the action the frontend passes to the backend for
 * execution.<p>
 * 
 * For simple CRUD-Transactions there exist shortcuts in the abstract Backend
 * class.<p>
 *
 * @param <T> type of return value.
 * (should extend Serializable, but thats not enforced by 'extends Serializable'
 * because signatures of methods get complicated by that and Void-Transactions
 * would not be possible because Void is not Serializable!)
 */
@FunctionalInterface
public interface Transaction<T> extends Serializable {

	/**
	 * The invocation method for the backend. Application code should not need
	 * to call this method directly.
	 * 
	 * @return the return value from the transaction
	 */
	public T execute();

	/**
	 * The default role for a Transaction relies on the Annotation
	 * on the class or on the package
	 * 
	 * @return the role needed to execute this transaction
	 */
	public default Role getRole() {
		Role role = getClass().getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		role = getClass().getPackage().getAnnotation(Role.class);
		return role;
	}

	/**
	 * The default isolation for a Transaction relies on the Annotation
	 * on the class
	 * 
	 * @return the used isolation (for example 'serializable') for transaction
	 */
	public default Isolation getIsolation() {
		return getClass().getAnnotation(Isolation.class);
	}
	
}
