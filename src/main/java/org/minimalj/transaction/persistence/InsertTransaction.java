package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.SerializationContainer;

@SuppressWarnings("unchecked")
public class InsertTransaction<T> implements Transaction<Object> {
	private static final long serialVersionUID = 1L;

	private final T object;
	
	public InsertTransaction(Object object) {
		this.object = (T) SerializationContainer.wrap(object);
	}

	@Override
	public Object execute(Persistence persistence) {
		return persistence.insert(SerializationContainer.unwrap(object));
	}

}