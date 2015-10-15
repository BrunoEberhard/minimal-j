package org.minimalj.transaction;

import org.minimalj.backend.Persistence;

public interface PersistenceTransaction<T> extends Transaction<T> {

	@Override
	public default T execute() {
		throw new RuntimeException(this.getClass().getSimpleName() + " must be executed with persistence argument");
	}
	
	/**
	 * Caution: Normally you don't need to override this method
	 * 
	 * @return The returned class is used to find the needed roles for this
	 * transaction to pass permission check.
	 */
	public default Class<?> getEntityClazz() {
		return null;
	}
	
	/**
	 * The invocation method for the backend. Application code should not need
	 * to call this method directly.
	 * 
	 * @param persistence the persistence this transaction should use
	 * @return the return value from the transaction
	 */
	public T execute(Persistence persistence);
	
}
