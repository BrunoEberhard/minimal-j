package org.minimalj.transaction.persistence;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.SerializationContainer;

public class InsertTransaction implements Transaction<Object> {
	private static final long serialVersionUID = 1L;

	private final Object object;

	public InsertTransaction(Object object) {
		this.object = SerializationContainer.wrap(object);
	}

	@Override
	public Object execute(Backend backend) {
		return backend.insert(SerializationContainer.unwrap(object));
	}

}