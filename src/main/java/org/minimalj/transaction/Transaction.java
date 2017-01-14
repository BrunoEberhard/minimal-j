package org.minimalj.transaction;

import java.io.Serializable;

import org.minimalj.backend.repository.EntityTransaction;

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
	 * The isolation for a Transaction relies on the Annotation
	 * on the class
	 * 
	 * @return the used isolation (for example 'serializable') for transaction
	 */	
	public static Isolation getIsolation(Transaction<?> transaction) {
		if (transaction instanceof EntityTransaction) {
			return ((EntityTransaction<?,?>) transaction).getEntityClazz().getAnnotation(Isolation.class);
		} else {
			return transaction.getClass().getAnnotation(Isolation.class);
		}
	}

	/**
	 * The role(s) for a Transaction relies on the Annotation
	 * on the class or on the package
	 * 
	 * @return the role needed to execute this transaction
	 */
	public static Role getRole(Transaction<?> transaction) {
		Role role;
		role = transaction.getClass().getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		role = transaction.getClass().getPackage().getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		if (transaction instanceof EntityTransaction) {
			EntityTransaction<?, ?> entityTransaction = (EntityTransaction<?, ?>) transaction;
			role = entityTransaction.getEntityClazz().getAnnotation(Role.class);
			if (role != null) {
				return role;
			}
			role = entityTransaction.getEntityClazz().getPackage().getAnnotation(Role.class);
			if (role != null) {
				return role;
			}
		}
		return null;
	}
	
}
