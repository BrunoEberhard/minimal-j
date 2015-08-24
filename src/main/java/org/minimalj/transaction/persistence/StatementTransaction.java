package org.minimalj.transaction.persistence;

import java.io.Serializable;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public class StatementTransaction<T> implements Transaction<T> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final String queryName;
	private final Serializable[] parameters;
	private final int maxResults;

	public StatementTransaction(Class<T> clazz, String queryName, Serializable[] parameters) {
		this(clazz, queryName, 0, parameters);
	}
	
	public StatementTransaction(Class<T> clazz, String queryName, int maxResults, Serializable[] parameters) {
		this.clazz = clazz;
		this.queryName = queryName;
		this.parameters = parameters;
		this.maxResults = maxResults;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T execute(Persistence persistence) {
		T result;
		if (maxResults > 0) {
			result = (T) persistence.executeStatement(clazz, queryName, maxResults, parameters);
		} else {
			result = persistence.executeStatement(clazz, queryName, parameters);
		}
		return result;
	}

}