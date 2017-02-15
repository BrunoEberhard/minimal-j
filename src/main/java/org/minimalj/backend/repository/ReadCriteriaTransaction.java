package org.minimalj.backend.repository;

import java.util.List;

import org.minimalj.repository.Repository;
import org.minimalj.repository.criteria.Query;
import org.minimalj.repository.criteria.Sorting;

public class ReadCriteriaTransaction<ENTITY> extends ReadTransaction<ENTITY, List<ENTITY>> {
	private static final long serialVersionUID = 1L;

	private final Query criteria;
	private final Sorting[] sorting;
	
	public ReadCriteriaTransaction(Class<ENTITY> clazz, Query criteria, Sorting... sorting) {
		super(clazz);
		this.criteria = criteria;
		this.sorting = sorting;
	}

	@Override
	public List<ENTITY> execute(Repository repository) {
		List<ENTITY> result = repository.find(getEntityClazz(), criteria, sorting);
		return result;
	}
}