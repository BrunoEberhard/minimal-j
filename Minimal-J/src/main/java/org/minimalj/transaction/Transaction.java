package org.minimalj.transaction;

import java.io.Serializable;

import org.minimalj.backend.Backend;

/**
 * The transaction is the action the frontend passes to the backend for
 * execution.<p>
 * 
 * For simple CRUD-Transactions there exist shortcuts in the backend
 * interface.<p>
 *
 * @param <T> type of return value. Has to be an extension of Serializable.
 * Sadly <code>Void</code> is not serializable. Use Integer if you don't have a return value
 * and return null from the execute method.
 */
public interface Transaction<T extends Serializable> extends Serializable {

	/**
	 * The invocation method for the backend. Application code should not need
	 * to call this method directly.
	 * 
	 * @param backend if the transcation needs things from the Backend this
	 * provides the access.
	 * @return the return value from the transaction
	 */
	public T execute(Backend backend);
	
}
