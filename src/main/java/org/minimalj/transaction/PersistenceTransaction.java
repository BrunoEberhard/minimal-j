package org.minimalj.transaction;

import java.sql.Connection;

import org.minimalj.backend.Backend;
import org.minimalj.backend.Persistence;

public abstract class PersistenceTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	@Override
	public final RETURN execute() {
		RETURN result;
		boolean commit = false;
		Persistence persistence = Backend.getPersistence();
		try {
			persistence.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
			result = execute(persistence);
			commit = true;
		} finally {
			persistence.endTransaction(commit);
		}
		return result;
	}

	protected abstract RETURN execute(Persistence persistence);
	
}
