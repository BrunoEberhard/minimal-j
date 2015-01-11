package org.minimalj.transaction.persistence;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.criteria.Criteria;

public class ReadCriteriaTransaction<T> implements Transaction<List<T>> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final Criteria criteria;
	private final int maxResults;
	
	public ReadCriteriaTransaction(Class<T> clazz, Criteria criteria, int maxResults) {
		this.clazz = clazz;
		this.criteria = criteria;
		this.maxResults = maxResults;
	}

	@Override
	public List<T> execute(Backend backend) {
		List<T>	result = backend.read(clazz, criteria, maxResults);
		return result;
	}

}