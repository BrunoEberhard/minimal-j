package org.minimalj.backend.persistence;

import org.minimalj.util.ClassHolder;

public abstract class ReadTransaction<ENTITY, RETURN> extends PersistenceTransaction<ENTITY, RETURN> {
	private static final long serialVersionUID = 1L;

	private final ClassHolder<ENTITY> classHolder;

	public ReadTransaction(Class<ENTITY> clazz) {
		this.classHolder = new ClassHolder<ENTITY>(clazz);
	}

	protected Class<ENTITY> getEntityClazz() {
		return classHolder.getClazz();
	}
}