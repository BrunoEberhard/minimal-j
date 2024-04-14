package org.minimalj.repository.list;

import org.minimalj.repository.query.Criteria;

/**
 * A RelationCriteria is a special Criteria that is used internally by
 * RelationLists that need to load additional element entities.
 *
 */
public class RelationCriteria extends Criteria {
	private static final long serialVersionUID = 1L;

	private final String crossName;
	private final Object relatedId;

	RelationCriteria(String crossName, Object relatedId) {
		this.crossName = crossName;
		this.relatedId = relatedId;
	}

	public String getCrossName() {
		return crossName;
	}

	public Object getRelatedId() {
		return relatedId;
	}
	
	@Override
	public boolean test(Object t) {
		throw new RuntimeException("Not supported");
	}
}
