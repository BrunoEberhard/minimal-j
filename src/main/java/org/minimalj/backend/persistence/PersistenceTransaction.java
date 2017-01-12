package org.minimalj.backend.persistence;

import java.sql.Connection;

import org.minimalj.backend.Backend;
import org.minimalj.persistence.Repository;
import org.minimalj.transaction.Transaction;

public abstract class PersistenceTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	@Override
	public final RETURN execute() {
		RETURN result;
		boolean commit = false;
		Repository repository = Backend.getInstance().getRepository();
		try {
			repository.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
			result = execute(repository);
			commit = true;
		} finally {
			repository.endTransaction(commit);
		}
		return result;
	}

	protected abstract RETURN execute(Repository repository);
	
	public abstract Class<ENTITY> getEntityClazz();

}
