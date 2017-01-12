package org.minimalj.backend.persistence;

import java.util.List;

import org.minimalj.persistence.Repository;
import org.minimalj.persistence.sql.SqlRepository;
import org.minimalj.util.IdUtils;

public class ReadHistoryTransaction<ENTITY> extends ReadTransaction<ENTITY, List<ENTITY>> {
	private static final long serialVersionUID = 1L;

	private final Object id;
	private final int maxResults;

	@SuppressWarnings("unchecked")
	public ReadHistoryTransaction(ENTITY object) {
		super((Class<ENTITY>) object.getClass());
		this.id = IdUtils.getId(object);
		this.maxResults = Integer.MAX_VALUE;
	}
	
	public ReadHistoryTransaction(Class<ENTITY> clazz, Object id) {
		this(clazz, id, Integer.MAX_VALUE);
	}
	
	public ReadHistoryTransaction(Class<ENTITY> clazz, Object id, int maxResults) {
		super(clazz);
		this.id = id;
		this.maxResults = maxResults;
	}
	
	@Override
	public List<ENTITY> execute(Repository repository) {
		if (repository instanceof SqlRepository) {
			SqlRepository sqlRepository = (SqlRepository) repository;
			List<ENTITY>	result = sqlRepository.loadHistory(getEntityClazz(), id, maxResults);
			return result;
		} else {
			throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlRepository.class.getSimpleName());
		}
	}

}