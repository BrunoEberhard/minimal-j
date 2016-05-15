package org.minimalj.transaction.persistence;

import java.util.List;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.util.IdUtils;

public class ReadHistoryTransaction<ENTITY> extends ClassPersistenceTransaction<ENTITY, List<ENTITY>> {
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
	public List<ENTITY> execute(Persistence persistence) {
		if (persistence instanceof SqlPersistence) {
			SqlPersistence sqlPersistence = (SqlPersistence) persistence;
			List<ENTITY>	result = sqlPersistence.loadHistory(getEntityClazz(), id, maxResults);
			return result;
		} else {
			throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlPersistence.class.getSimpleName());
		}
	}

}