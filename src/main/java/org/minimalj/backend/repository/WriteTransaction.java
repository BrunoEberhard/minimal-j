package org.minimalj.backend.repository;

import org.minimalj.util.SerializationContainer;

public abstract class WriteTransaction<ENTITY, RETURN> extends EntityTransaction<ENTITY, RETURN> {
	private static final long serialVersionUID = 1L;

	private final Object object;
	private transient ENTITY unwrapped;

	public WriteTransaction(ENTITY object) {
		this.object = SerializationContainer.wrap(object);
	}

	@SuppressWarnings("unchecked")
	protected ENTITY getUnwrapped() {
		if (unwrapped == null) {
			unwrapped = (ENTITY) SerializationContainer.unwrap(object);
		}
		return unwrapped;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<ENTITY> getEntityClazz() {
		return (Class<ENTITY>) getUnwrapped().getClass();
	}

}