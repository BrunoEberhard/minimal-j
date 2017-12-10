package org.minimalj.backend.repository;

import org.minimalj.repository.Repository;
import org.minimalj.util.Codes;
import org.minimalj.util.IdUtils;

public class SaveTransaction<ENTITY> extends WriteTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	public SaveTransaction(ENTITY object) {
		super(object);
	}

	@Override
	protected ENTITY execute(Repository repository) {
		ENTITY unwrapped = getUnwrapped();
		Object id = IdUtils.getId(unwrapped); 
		if (id == null) {
			id = repository.insert(unwrapped);
		} else {
			if (Codes.isCode(getEntityClazz())) {
				Object existing = repository.read(getEntityClazz(), id);
				if (existing == null) {
					repository.insert(unwrapped);
				} else {
					repository.update(unwrapped);
				}
			} else {
				repository.update(unwrapped);
			}
		}
		return (ENTITY) repository.read(unwrapped.getClass(), id);
	}
}