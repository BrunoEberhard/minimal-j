package org.minimalj.backend.repository;

import org.minimalj.repository.Repository;
import org.minimalj.repository.sql.SqlHistorizedRepository;
import org.minimalj.repository.sql.SqlRepository;

public class ReadHistorizedEntityTransaction<ENTITY> extends ReadTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	private final Object id;
	private final Integer time;

	public ReadHistorizedEntityTransaction(Class<ENTITY> clazz, Object id, Integer time) {
		super(clazz);
		this.id = id;
		this.time = time;
	}

	@Override
	public ENTITY execute() {
		Repository repository = repository();
		ENTITY result;
		if (time == null) {
			result = repository.read(getEntityClazz(), id);
		} else {
			if (repository instanceof SqlHistorizedRepository) {
				SqlHistorizedRepository sqlRepository = (SqlHistorizedRepository) repository;
				result = sqlRepository.readVersion(getEntityClazz(), id, time);
			} else {
				throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlRepository.class.getSimpleName());
			}
		}
		return result;
	}

}