package org.minimalj.transaction.persistence;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.SerializationContainer;

@SuppressWarnings("unchecked")
public class InsertTransaction<T> implements Transaction<T> {
	private static final long serialVersionUID = 1L;

	private final T object;
	
	public InsertTransaction(Object object) {
		this.object = (T) SerializationContainer.wrap(object);
	}

	@Override
	public T execute(Backend backend) {
		return (T) backend.insert(SerializationContainer.unwrap(object));
	}

}