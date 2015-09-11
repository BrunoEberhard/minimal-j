package org.minimalj.transaction;

import java.io.Serializable;

import org.minimalj.backend.Persistence;

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
public interface Transaction<T> extends Serializable {

	/**
	 * The invocation method for the backend. Application code should not need
	 * to call this method directly.
	 * 
	 * @param persistence the persistence this transaction should use
	 * @return the return value from the transaction
	 */
	public T execute(Persistence persistence);
	
}
