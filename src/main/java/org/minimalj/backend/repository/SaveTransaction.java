package org.minimalj.backend.repository;

import org.minimalj.model.properties.Properties;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.By;
import org.minimalj.util.Codes;
import org.minimalj.util.IdUtils;

public class SaveTransaction<ENTITY> extends WriteTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	public SaveTransaction(ENTITY object) {
		super(object);
	}

	@Override
	public ENTITY execute() {
		Repository repository = repository();
		ENTITY unwrapped = getUnwrapped();
		Object id = IdUtils.getId(unwrapped); 
		if (id == null) {
			id = repository.insert(unwrapped);
		} else {
			if (Codes.isCode(getEntityClazz())) {
				boolean existing = repository.count(getEntityClazz(), By.field(Properties.getProperty(getEntityClazz(), "id"), id)) > 0;
				if (!existing) {
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