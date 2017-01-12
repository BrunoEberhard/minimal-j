package org.minimalj.backend.persistence;

import org.minimalj.persistence.Repository;
import org.minimalj.persistence.sql.SqlRepository;

public class ReadEntityTransaction<ENTITY> extends ReadTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	private final Object id;
	private final Integer time;

	public ReadEntityTransaction(Class<ENTITY> clazz, Object id) {
		this(clazz, id, null);
	}
	
	public ReadEntityTransaction(Class<ENTITY> clazz, Object id, Integer time) {
		super(clazz);
		this.id = id;
		this.time = time;
	}

	@Override
	protected ENTITY execute(Repository repository) {
		ENTITY result;
		if (time == null) {
			result = repository.read(getEntityClazz(), id);
		} else {
			if (repository instanceof SqlRepository) {
				SqlRepository sqlRepository = (SqlRepository) repository;
				result = sqlRepository.readVersion(getEntityClazz(), id, time);
			} else {
				throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlRepository.class.getSimpleName());
			}
		}
		return result;
	}

}