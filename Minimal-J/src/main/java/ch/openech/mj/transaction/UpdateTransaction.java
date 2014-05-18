package ch.openech.mj.transaction;

import java.io.Serializable;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.util.SerializationContainer;

public class UpdateTransaction implements Transaction<Serializable> {
	private static final long serialVersionUID = 1L;

	private final Object object;

	public UpdateTransaction(Object object) {
		this.object = SerializationContainer.wrap(object);
	}

	@Override
	public Serializable execute(Backend backend) {
		backend.update(SerializationContainer.unwrap(object));
		return null;
	}

}