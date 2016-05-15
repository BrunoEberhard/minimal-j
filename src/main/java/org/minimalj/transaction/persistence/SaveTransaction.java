package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.util.IdUtils;

public class SaveTransaction<ENTITY> extends ObjectPersistenceTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	public SaveTransaction(ENTITY object) {
		super(object);
	}

	@Override
	protected ENTITY execute(Persistence persistence) {
		ENTITY unwrapped = getUnwrapped();
		Object id = IdUtils.getId(unwrapped, !IdUtils.PLAIN); 
		if (id == null) {
			id = persistence.insert(unwrapped);
		} else {
			persistence.update(unwrapped);
		}
		return (ENTITY) persistence.read(getEntityClazz(), id);
	}
}