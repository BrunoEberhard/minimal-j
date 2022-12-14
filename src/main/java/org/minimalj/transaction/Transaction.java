package org.minimalj.transaction;

import java.io.Serializable;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.sql.SqlRepository;
import org.minimalj.security.AccessControl;
import org.minimalj.security.Authorization;
import org.minimalj.security.Subject;

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
public interface Transaction<RETURN> extends AccessControl, Serializable {

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
	
	default SqlRepository sqlRepository() {
		return (SqlRepository) repository();
	}
	
	default String $(Object classOrKey) {
		return sqlRepository().name(classOrKey);
	}

	default <T> T read(Class<T> clazz, Object id) {
		return repository().read(clazz, id);
	}

	default <T> List<T> find(Class<T> clazz, Query query) {
		return repository().find(clazz, query);
	}

	default <T> long count(Class<T> clazz, Criteria criteria) {
		return repository().count(clazz, criteria);
	}

	default <T> Object insert(T object) {
		return repository().insert(object);
	}

	default <T> void update(T object) {
		repository().update(object);
	}

	default <T> void delete(Class<T> clazz, Criteria criteria) {
		repository().delete(clazz, criteria);
	}
	
	default Isolation.Level getIsolation() {
		Isolation isolation = AnnotationUtil.getAnnotationOfClassOrPackage(getClass(), Isolation.class);
		return isolation != null ? isolation.value() : Isolation.Level.SERIALIZABLE;
	}

	@Override
	default boolean hasAccess(Subject subject) {
		return !Boolean.FALSE.equals(Authorization.hasAccessByAnnotation(subject, this.getClass()));
	}
}