package org.minimalj.transaction;

import java.sql.Connection;

import org.minimalj.backend.Persistence;

public abstract class PersistenceTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	@Override
	public final RETURN execute() {
		RETURN result;
		boolean commit = false;
		Persistence persistence = Persistence.getInstance();
		try {
			persistence.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
			result = execute(persistence);
			commit = true;
		} finally {
			persistence.endTransaction(commit);
		}
		return result;
	}

//	public List<String> getRoles() {
//		List<String> neededRoles = new ArrayList<>();
//		PersistenceRole[] roles = getEntityClazz().getAnnotationsByType(PersistenceRole.class);
//		checkPermissions(subject, roles);
//
//		roles = getEntityClazz().getPackage().getAnnotationsByType(PersistenceRole.class);
//		checkPermissions(subject, roles);
//	}
//
//	Das Problem ist, dass mehrere And von Ors Verknüpfungen entstehen.
//	
//	private void getRoles(List<String> neededRoles, PersistenceRole[] roles) {
//		for (PersistenceRole role : roles) {
//			if (role.type().isAssignableFrom(this.getClass())) {
//				neededRoles.add(role.)
//			}
//		}
//	}
//	
	
	protected abstract RETURN execute(Persistence persistence);
	
}
