package org.minimalj.repository.query;

public class RelationCriteria extends Criteria {

	private static final long serialVersionUID = 1L;
	
	private final String crossName;
	private final Object relatedId;

	public RelationCriteria(String crossName, Object relatedId) {
		super();
		this.crossName = crossName;
		this.relatedId = relatedId;
	}

	public String getCrossName() {
		return crossName;
	}

	public Object getRelatedId() {
		return relatedId;
	}
}
