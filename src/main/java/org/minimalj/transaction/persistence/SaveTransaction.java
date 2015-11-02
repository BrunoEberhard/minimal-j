package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.util.IdUtils;

public class SaveTransaction<T> extends BasePersistenceTransaction<T> {
	private static final long serialVersionUID = 1L;

	public SaveTransaction(T object) {
		super(object);
	}

	@Override
	public T execute(Persistence persistence) {
		T unwrapped = getUnwrapped();
		Object id = IdUtils.getId(unwrapped); 
		if (id == null) {
			id = persistence.insert(unwrapped);
		} else {
			persistence.update(unwrapped);
		}
		return (T) persistence.read(getEntityClazz(), id);
	}
}