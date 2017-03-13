package org.minimalj.repository.query;

import org.minimalj.util.ClassHolder;

@SuppressWarnings("rawtypes")
public class RelationCriteria extends Criteria {

	private static final long serialVersionUID = 1L;
	
	private final ClassHolder relatedClass;
	private final String crossName;
	private final Object relatedId;

	@SuppressWarnings("unchecked")
	public RelationCriteria(Class relatedClass, String crossName, Object relatedId) {
		super();
		this.relatedClass = new ClassHolder(relatedClass);
		this.crossName = crossName;
		this.relatedId = relatedId;
	}

	public Class getRelatedClazz() {
		return relatedClass.getClazz();
	}
	
	public String getCrossName() {
		return crossName;
	}

	public Object getRelatedId() {
		return relatedId;
	}
}
