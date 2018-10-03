package org.minimalj.backend.repository;

import java.util.List;

import org.minimalj.repository.query.Query;

public class ReadCriteriaTransaction<ENTITY> extends ReadTransaction<ENTITY, List<ENTITY>> {
	private static final long serialVersionUID = 1L;

	private final Query criteria;
	
	public ReadCriteriaTransaction(Class<ENTITY> clazz, Query criteria) {
		super(clazz);
		this.criteria = criteria;
	}

	@Override
	public List<ENTITY> execute() {
		List<ENTITY> result = find(getEntityClazz(), criteria);
		return result;
	}
}