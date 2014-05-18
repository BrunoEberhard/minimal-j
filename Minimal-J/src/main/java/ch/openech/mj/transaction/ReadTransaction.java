package ch.openech.mj.transaction;

import java.io.Serializable;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.util.SerializationContainer;

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
		return (Serializable) SerializationContainer.wrap(result);
	}

}