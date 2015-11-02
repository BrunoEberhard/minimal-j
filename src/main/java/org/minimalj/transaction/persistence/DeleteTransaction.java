package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.util.IdUtils;

public class DeleteTransaction implements PersistenceTransaction<Void> {
	private static final long serialVersionUID = 1L;

	private final Object id;
	private final String className;
	private transient Class<?> clazz;

	public DeleteTransaction(Object object) {
		this(object.getClass(), IdUtils.getId(object));
	}

	public DeleteTransaction(Class<?> clazz, Object id) {
		this.clazz = clazz;
		this.id = id;
		this.className = clazz.getName();
	}
	
	@Override
	public Class<?> getEntityClazz() {
		if (clazz == null) {
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return clazz;
	}
	
	@Override
	public Void execute(Persistence persistence) {
		persistence.delete(getEntityClazz(), id);
		return null;
	}

}