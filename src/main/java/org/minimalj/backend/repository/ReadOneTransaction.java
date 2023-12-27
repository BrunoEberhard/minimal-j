package org.minimalj.backend.repository;

import java.util.List;

import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Query;

public class ReadOneTransaction<ENTITY> extends ReadTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	private final Query criteria;
	private final boolean strictOne;
	
	public ReadOneTransaction(Class<ENTITY> clazz, Query criteria, boolean strictOne) {
		super(clazz);
		if (criteria instanceof Limit) {
			throw new IllegalArgumentException("To find first or one entity not Limit must be specified");
		}
		this.criteria = criteria;
		this.strictOne = strictOne;
	}

	@Override
	public ENTITY execute() {
		Limit limit = new Limit(criteria, strictOne ? 2 : 1);
		List<ENTITY> result = find(getEntityClazz(), limit);
		int size = result.size();
		if (size == 0) {
			return null;
		} else if (size > 1 && strictOne) {
			throw new IllegalArgumentException("Found more than one entity for " + criteria.toString());
		} else {
			return result.get(0);
		}
	}
	
	@Override
	public String toString() {
		return "Read " + (strictOne ? "" : "max ") + "one " + getEntityClazz().getSimpleName() + " by " + criteria;
	}
}