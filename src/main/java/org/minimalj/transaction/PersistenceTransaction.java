package org.minimalj.transaction;

import org.minimalj.backend.Persistence;
import org.minimalj.security.Subject;

public abstract class PersistenceTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	@Override
	public RETURN execute() {
		checkPermissions();
		
		Persistence persistence = Persistence.getInstance();
		return execute(persistence);
	}
	
	private void checkPermissions() {
		Role role = findRoleByType(getEntityClazz(), getClass());
		if (role != null && !Subject.hasRole(role.value())) {
			throw new NotAuthorizedException();
		}
	}

	private static Role findRoleByType(Class<?> clazz, @SuppressWarnings("rawtypes") Class<? extends Transaction> transactionClass) {
		Role[] roles = clazz.getAnnotationsByType(Role.class);
		Role role = findRoleByType(roles, transactionClass);
		if (role != null) {
			return role;
		}
		roles = clazz.getPackage().getAnnotationsByType(Role.class);
		role = findRoleByType(roles, transactionClass);
		return role;
	}
	
	private static Role findRoleByType(Role[] roles, @SuppressWarnings("rawtypes") Class<? extends Transaction> transactionClass) {
		for (Role role : roles) {
			if (role.transaction() == transactionClass) {
				return role;
			}
		}
		// TODO respect class hierachy when retrieving needed role for a transaction
		// the following lines only go by order of the roles not by the hierachy
//		for (Role role : roles) {
//			if (role.transactionClass().isAssignableFrom(transactionClass)) {
//				return role;
//			}
//		}
		
		// check for the Transaction.class as this is the default in the annotation
		for (Role role : roles) {
			if (role.transaction() == Transaction.class) {
				return role;
			}
		}		
		return null;
	}
	
	protected abstract Class<ENTITY> getEntityClazz();
	
	protected abstract RETURN execute(Persistence persistence);
	
}
