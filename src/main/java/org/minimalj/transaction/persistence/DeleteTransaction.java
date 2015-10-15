package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.util.IdUtils;

public class DeleteTransaction implements PersistenceTransaction<Void> {
	private static final long serialVersionUID = 1L;

	private final Class<?> clazz;
	private final Object id;

	public DeleteTransaction(Object object) {
		this(object.getClass(), IdUtils.getId(object));
	}

	public DeleteTransaction(Class<?> clazz, Object id) {
		this.clazz = clazz;
		this.id = id;
	}
	
	@Override
	public Class<?> getEntityClazz() {
		return clazz;
	}
	
	@Override
	public Void execute(Persistence persistence) {
		persistence.delete(clazz, id);
		return null;
	}

}