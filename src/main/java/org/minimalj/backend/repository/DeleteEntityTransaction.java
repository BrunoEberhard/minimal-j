package org.minimalj.backend.repository;

import java.util.Objects;

import org.minimalj.util.ClassHolder;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;

public class DeleteEntityTransaction<ENTITY> extends EntityTransaction<ENTITY, Void> {
	private static final long serialVersionUID = 1L;

	private final ClassHolder<ENTITY> classHolder;
	private final Object id;
	private final Integer version;
	private final transient Object object;
	
	@Override
	public Class<ENTITY> getEntityClazz() {
		return classHolder.getClazz();
	}
	
	public DeleteEntityTransaction(ENTITY object) {
		Objects.requireNonNull(object);
		this.classHolder = new ClassHolder<>((Class<ENTITY>) object.getClass());
		this.object = object;
		this.id = IdUtils.getId(object);
		this.version = FieldUtils.hasValidHistorizedField(object.getClass()) ? IdUtils.getVersion(object) : null;
	}

	@Override
	public Void execute() {
		if (object != null) {
			repository().delete(object);
		} else {
			ENTITY idOnly = CloneHelper.newInstance(getEntityClazz());
			IdUtils.setId(idOnly, id);
			if (version != null) {
				IdUtils.setVersion(idOnly, version);
			}
			repository().delete(idOnly);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Delete " + classHolder + " " + id;
	}
}