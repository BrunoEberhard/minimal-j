package org.minimalj.backend.repository;

import java.util.Objects;

import org.minimalj.backend.Backend;
import org.minimalj.repository.query.Criteria;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.ClassHolder;

public class DeleteTransaction<ENTITY> implements Transaction<Integer> {
	private static final long serialVersionUID = 1L;

	private final ClassHolder<ENTITY> classHolder;
	private final Criteria criteria;

	public DeleteTransaction(Class<ENTITY> clazz, Criteria criteria) {
		Objects.nonNull(criteria);
		this.criteria = criteria;
		this.classHolder = new ClassHolder<>(clazz);
	}

	@Override
	public Integer execute() {
		return Backend.getInstance().getRepository().delete(classHolder.getClazz(), criteria);
	}
}