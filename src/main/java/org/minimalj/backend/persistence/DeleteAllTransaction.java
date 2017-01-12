package org.minimalj.backend.persistence;

import org.minimalj.persistence.Repository;
import org.minimalj.persistence.sql.SqlRepository;

public class DeleteAllTransaction<ENTITY> extends DeleteEntityTransaction<ENTITY> {
	private static final long serialVersionUID = 1L;

	public DeleteAllTransaction(final Class<ENTITY> clazz) {
		super(clazz);
	}
	
	@Override
	public Void execute(Repository repository) {
		if (repository instanceof SqlRepository) {
			SqlRepository sqlRepository = (SqlRepository) repository;
			sqlRepository.deleteAll(getEntityClazz());
			return null;
		} else {
			throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlRepository.class.getSimpleName());
		}
	}

}