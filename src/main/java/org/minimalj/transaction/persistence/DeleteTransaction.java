package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.util.IdUtils;

public class DeleteTransaction<ENTITY> extends ClassPersistenceTransaction<ENTITY, Void> {
	private static final long serialVersionUID = 1L;

	private final Object id;

	public DeleteTransaction(ENTITY object) {
		this((Class<ENTITY>) object.getClass(), IdUtils.getId(object));
	}

	public DeleteTransaction(Class<ENTITY> clazz, Object id) {
		super(clazz);
		this.id = id;
	}
	
	@Override
	public Void execute(Persistence persistence) {
		persistence.delete(getEntityClazz(), id);
		return null;
	}

}