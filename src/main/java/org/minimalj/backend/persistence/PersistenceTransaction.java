package org.minimalj.backend.persistence;

import java.sql.Connection;

import org.minimalj.backend.Backend;
import org.minimalj.persistence.Persistence;
import org.minimalj.transaction.Transaction;

public abstract class PersistenceTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	@Override
	public final RETURN execute() {
		RETURN result;
		boolean commit = false;
		Persistence persistence = Backend.getInstance().getPersistence();
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
