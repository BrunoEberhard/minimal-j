package org.minimalj.transaction.persistence;

import java.io.Serializable;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.Transaction;

public class DeleteAllTransaction implements Transaction<Serializable> {
	private static final long serialVersionUID = 1L;

	private final Class<?> clazz;

	public DeleteAllTransaction(final Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Serializable execute(Backend backend) {
		backend.deleteAll(clazz);
		return null;
	}

}