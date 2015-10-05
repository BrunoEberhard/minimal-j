package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.SerializationContainer;

public class UpdateTransaction<T> implements Transaction<T> {
	private static final long serialVersionUID = 1L;

	private final Object object;
	
	public UpdateTransaction(T object) {
		this.object = SerializationContainer.wrap(object);
	}

	@Override
	public TransactionType getType() {
		return TransactionType.UPDATE;
	}
	
	@Override
	public Class<?> getClazz() {
		return SerializationContainer.unwrap(object).getClass();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T execute(Persistence persistence) {
		return (T) persistence.update(SerializationContainer.unwrap(object));
	}

}