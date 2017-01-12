package org.minimalj.backend.persistence;

import java.util.List;

import org.minimalj.persistence.Repository;
import org.minimalj.persistence.criteria.Criteria;

public class ReadCriteriaTransaction<ENTITY> extends ReadTransaction<ENTITY, List<ENTITY>> {
	private static final long serialVersionUID = 1L;

	private final Criteria criteria;
	private final int maxResults;
	
	public ReadCriteriaTransaction(Class<ENTITY> clazz, Criteria criteria, int maxResults) {
		super(clazz);
		this.criteria = criteria;
		this.maxResults = maxResults;
	}

	@Override
	public List<ENTITY> execute(Repository repository) {
		List<ENTITY> result = repository.read(getEntityClazz(), criteria, maxResults);
		return result;
	}
}