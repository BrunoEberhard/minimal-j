package ch.openech.mj.transaction;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.util.SerializationContainer;

public class InsertTransaction implements Transaction<Long> {
	private static final long serialVersionUID = 1L;

	private final Object object;

	public InsertTransaction(Object object) {
		this.object = SerializationContainer.wrap(object);
	}

	@Override
	public Long execute(Backend backend) {
		return backend.insert(SerializationContainer.unwrap(object));
	}

}