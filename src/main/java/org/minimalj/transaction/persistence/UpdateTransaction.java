package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.SerializationContainer;

@SuppressWarnings("unchecked")
public class UpdateTransaction<T> implements Transaction<T> {
	private static final long serialVersionUID = 1L;

	private final T object;
	
	public UpdateTransaction(T object) {
		this.object = (T) SerializationContainer.wrap(object);
	}

	@Override
	public T execute(Persistence persistence) {
		return (T) persistence.update(SerializationContainer.unwrap(object));
	}

}