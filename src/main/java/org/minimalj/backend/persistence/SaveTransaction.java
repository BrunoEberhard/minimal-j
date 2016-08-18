package org.minimalj.backend.persistence;

import org.minimalj.persistence.Persistence;
import org.minimalj.util.IdUtils;

public class SaveTransaction<ENTITY> extends WriteTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	public SaveTransaction(ENTITY object) {
		super(object);
	}

	@Override
	protected ENTITY execute(Persistence persistence) {
		ENTITY unwrapped = getUnwrapped();
		Object id = IdUtils.getId(unwrapped); 
		if (id == null) {
			id = persistence.insert(unwrapped);
		} else {
			persistence.update(unwrapped);
		}
		return (ENTITY) persistence.read(unwrapped.getClass(), id);
	}
}