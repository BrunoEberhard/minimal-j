package org.minimalj.repository;

public interface TransactionalRepository extends Repository {

	public void startTransaction(int transactionIsolationLevel);
	
	public void endTransaction(boolean commit);
	
}
