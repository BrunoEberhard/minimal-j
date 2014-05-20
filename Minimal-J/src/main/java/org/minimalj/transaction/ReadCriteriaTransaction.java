package org.minimalj.transaction;

import java.io.Serializable;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.SerializationContainer;

public class ReadCriteriaTransaction<T> implements Transaction<Serializable> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final Criteria criteria;

	public ReadCriteriaTransaction(Class<T> clazz, Criteria criteria) {
		this.clazz = clazz;
		this.criteria = criteria;
	}

	@Override
	public Serializable execute(Backend backend) {
		List<T>	result = backend.read(clazz, criteria);
		return SerializationContainer.wrap(result);
	}

}