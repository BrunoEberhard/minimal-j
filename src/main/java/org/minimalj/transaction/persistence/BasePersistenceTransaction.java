package org.minimalj.transaction.persistence;

import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.util.SerializationContainer;

public abstract class BasePersistenceTransaction<T> implements PersistenceTransaction<T> {

	private final Object object;
	private transient T unwrapped;

	public BasePersistenceTransaction(T object) {
		this.object = SerializationContainer.wrap(object);
	}

	@Override
	public Class<?> getEntityClazz() {
		return getUnwrapped().getClass();
	}
	
	protected T getUnwrapped() {
		if (unwrapped == null) {
			unwrapped = (T) SerializationContainer.unwrap(object);
		}
		return unwrapped;
	}
}