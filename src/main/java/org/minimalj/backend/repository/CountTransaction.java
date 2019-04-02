package org.minimalj.backend.repository;

import org.minimalj.repository.query.Criteria;

public class CountTransaction<ENTITY> extends ReadTransaction<ENTITY, Long> {
	private static final long serialVersionUID = 1L;

	private final Criteria criteria;
	
	public CountTransaction(Class<ENTITY> clazz, Criteria criteria) {
		super(clazz);
		this.criteria = criteria;
	}

	@Override
	public Long execute() {
		return count(getEntityClazz(), criteria);
	}
}