package org.minimalj.transaction.persistence;

import java.io.Serializable;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.SerializationContainer;

public class ReadTransaction<T> implements Transaction<Serializable> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final long id;
	private final Integer time;

	public ReadTransaction(Class<T> clazz, long id, Integer time) {
		this.clazz = clazz;
		this.id = id;
		this.time = time;
	}

	@Override
	public Serializable execute(Backend backend) {
		T result = backend.read(clazz, id, time);
		return SerializationContainer.wrap(result);
	}

}