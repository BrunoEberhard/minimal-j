package org.minimalj.transaction.persistence;

import java.io.Serializable;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public class ExecuteTransaction<T> implements Transaction<T> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final String query;
	private final Serializable[] parameters;
	private final int maxResults;

	public ExecuteTransaction(Class<T> clazz, String query, Serializable[] parameters) {
		this(clazz, query, 0, parameters);
	}
	
	public ExecuteTransaction(Class<T> clazz, String query, int maxResults, Serializable[] parameters) {
		this.clazz = clazz;
		this.query = query;
		this.parameters = parameters;
		this.maxResults = maxResults;
	}

	@Override
	public T execute(Persistence persistence) {
		T result;
		if (maxResults > 0) {
			// note: this compiles only because of type erasure
			result = persistence.execute(clazz, query, maxResults, parameters);
		} else {
			result = persistence.execute(clazz, query, parameters);
		}
		return result;
	}

}