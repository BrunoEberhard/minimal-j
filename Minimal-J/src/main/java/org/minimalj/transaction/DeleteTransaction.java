package org.minimalj.transaction;

import java.io.Serializable;

import org.minimalj.backend.Backend;
import org.minimalj.util.SerializationContainer;

public class DeleteTransaction implements Transaction<Serializable> {
	private static final long serialVersionUID = 1L;

	private final Object object;

	public DeleteTransaction(Object object) {
		this.object = SerializationContainer.wrap(object);
	}

	@Override
	public Serializable execute(Backend backend) {
		backend.delete(SerializationContainer.unwrap(object));
		return null;
	}

}