package org.minimalj.metamodel.model;

import java.util.ArrayList;
import java.util.List;

// TODO rename all MjXy classes to MetaXy
public class MjModel {

	public final List<MjEntity> entities = new ArrayList<>();
	
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
