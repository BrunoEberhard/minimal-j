package org.minimalj.metamodel.model;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Model;

public class MjModel {

	public final List<MjEntity> entities = new ArrayList<>();

	public MjModel(Model model) {
		this(model.getEntityClasses());
	}
	
	public MjModel(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			new MjEntity(this, clazz);
		}
	}

	public void addEntity(MjEntity mjEntity) {
		if (!entities.contains(mjEntity)) {
			entities.add(mjEntity);
		}
	}

	public MjEntity getEntity(Class<?> clazz) {
		for (MjEntity entity : entities) {
			if (entity.name.equals(clazz.getName())) {
				return entity;
			}
		}
		return new MjEntity(this, clazz);
	}
	
}
