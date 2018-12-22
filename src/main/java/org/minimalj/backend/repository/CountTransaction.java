package org.minimalj.backend.repository;

import org.minimalj.repository.query.Query;

public class CountTransaction<ENTITY> extends ReadTransaction<ENTITY, Long> {
	private static final long serialVersionUID = 1L;

	private final Query criteria;
	
	public CountTransaction(Class<ENTITY> clazz, Query criteria) {
		super(clazz);
		this.criteria = criteria;
	}

	@Override
	public Long execute() {
		return count(getEntityClazz(), criteria);
	}
}