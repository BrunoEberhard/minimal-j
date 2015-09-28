package org.minimalj.transaction.persistence;

import java.util.List;
import java.util.function.Predicate;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public class ReadCriteriaTransaction<T> implements Transaction<List<T>> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final Predicate<T> predicate;
	private final int maxResults;
	
	public ReadCriteriaTransaction(Class<T> clazz, Predicate<T> predicate, int maxResults) {
		this.clazz = clazz;
		this.predicate = predicate;
		this.maxResults = maxResults;
	}

	@Override
	public List<T> execute(Persistence persistence) {
		List<T>	result = persistence.read(clazz, predicate, maxResults);
		return result;
	}

}