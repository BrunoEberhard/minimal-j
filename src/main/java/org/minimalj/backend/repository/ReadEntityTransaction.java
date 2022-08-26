package org.minimalj.backend.repository;

public class ReadEntityTransaction<ENTITY> extends ReadTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	protected final Object id;

	public ReadEntityTransaction(Class<ENTITY> clazz, Object id) {
		super(clazz);
		this.id = id;
	}

	@Override
	public ENTITY execute() {
		ENTITY result = read(getEntityClazz(), id);
		return result;
	}

}