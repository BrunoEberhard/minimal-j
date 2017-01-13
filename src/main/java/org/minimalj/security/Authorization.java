package org.minimalj.security;

import java.util.Collections;
import java.util.List;

import org.minimalj.backend.repository.RepositoryTransaction;
import org.minimalj.frontend.action.Action;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;

/**
 * The 
 *
 */
public class Authorization {

	public final void check(Object object) {
		if (!isAllowed(object)) {
			throw new IllegalStateException(object + " forbidden");
		}
	}

	public final boolean isAllowed(Object object) {
		if (object instanceof RepositoryTransaction) {
			RepositoryTransaction<?, ?> repositoryTransaction = (RepositoryTransaction<?, ?>) object;
			return isAllowedRepositoryTransaction(repositoryTransaction);
		} else if (object instanceof Transaction) {
			Transaction<?> transaction = (Transaction<?>) object;
			return isAllowedTransaction(transaction);
		} else if (object instanceof Action) {
			Action action = (Action) object;
			return isAllowedAction(action);
		} else {
			return isAllowedEntity(object);
		}
	}

	//
	
	public boolean isAllowedEntity(Object entity) {
		return isAllowed(entity.getClass());
	}
	
	public boolean isAllowedAction(Action action) {
		return isAllowed(action.getClass());
	}
	
	public boolean isAllowedTransaction(Transaction<?> transaction) {
		return isAllowed(transaction.getClass());
	}
	
	public boolean isAllowedRepositoryTransaction(RepositoryTransaction<?, ?> transaction) {
		return isAllowed(transaction.getEntityClazz());
	}
	
	//

	public static boolean isAllowed(Class<?> clazz) {
		List<String> currentRoles = getCurrentRoles();
		return isAllowed(currentRoles, clazz);
	}
	
	public static boolean isAllowed(List<String> currentRoles, Class<?> clazz) {
		Role role = getRole(clazz);
		if (role != null) {
			for (String allowingRole : role.value()) {
				if (currentRoles.contains(allowingRole)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	public static Role getRole(Class<?> clazz) {
		Role role = clazz.getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		role = clazz.getPackage().getAnnotation(Role.class);
		return role;
	}
	
	protected static List<String> getCurrentRoles() {
		Subject subject = Subject.getCurrent();
		return subject != null ? subject.getRoles() : Collections.emptyList();
	}
	
}