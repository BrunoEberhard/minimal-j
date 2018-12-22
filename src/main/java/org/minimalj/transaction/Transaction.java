package org.minimalj.transaction;

import java.io.Serializable;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.Query;

/**
 * The transaction is the action the frontend passes to the backend for
 * execution.
 * <p>
 * 
 *
 * @param <RETURN> type of return value. (should extend Serializable, but thats
 *        not enforced by 'extends Serializable' because signatures of methods
 *        get complicated by that and Void-Transactions would not be possible
 *        because Void is not Serializable!)
 */
@FunctionalInterface
public interface Transaction<RETURN> extends Serializable {

	/**
	 * The invocation method for the backend. Application code should not need
	 * to call this method directly.
	 * 
	 * @return the return value from the transaction
	 */
	public RETURN execute();

	default Repository repository() {
		return Backend.getInstance().getRepository();
	}

	default <T> T read(Class<T> clazz, Object id) {
		return repository().read(clazz, id);
	}

	default <T> List<T> find(Class<T> clazz, Query query) {
		return repository().find(clazz, query);
	}

	default <T> long count(Class<T> clazz, Query query) {
		return repository().count(clazz, query);
	}

	default <T> Object insert(T object) {
		return repository().insert(object);
	}

	default <T> void update(T object) {
		repository().update(object);
	}

	default <T> void delete(Class<T> clazz, Object id) {
		repository().delete(clazz, id);
	}

}