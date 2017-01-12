package org.minimalj.backend.persistence;

import java.util.Objects;

import org.minimalj.persistence.Persistence;
import org.minimalj.util.ClassHolder;
import org.minimalj.util.IdUtils;

public class DeleteEntityTransaction<ENTITY> extends PersistenceTransaction<ENTITY, Void> {
	private static final long serialVersionUID = 1L;

	private final ClassHolder<ENTITY> classHolder;
	private final Object id;

	@Override
	public Class<ENTITY> getEntityClazz() {
		return classHolder.getClazz();
	}
	
	public DeleteEntityTransaction(ENTITY object) {
		Objects.nonNull(object);
		this.classHolder = new ClassHolder<ENTITY>((Class<ENTITY>) object.getClass());
		this.id = IdUtils.getId(object);
	}

	protected DeleteEntityTransaction(Class<ENTITY> clazz) {
		this.classHolder = new ClassHolder<ENTITY>(clazz);
		this.id = null;
	}
	
	public DeleteEntityTransaction(Class<ENTITY> clazz, Object id) {
		Objects.nonNull(clazz);
		Objects.nonNull(id);
		this.classHolder = new ClassHolder<ENTITY>(clazz);
		this.id = id;
	}

	@Override
	public Void execute(Persistence persistence) {
		persistence.delete(getEntityClazz(), id);
		return null;
	}
}