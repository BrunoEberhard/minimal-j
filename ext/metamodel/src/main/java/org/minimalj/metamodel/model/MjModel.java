package org.minimalj.metamodel.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.minimalj.model.Model;

public class MjModel {

	public final List<MjEntity> entities = new ArrayList<>();

	public MjModel(Model model) {
		this(model.getEntityClasses());
	}
	
	public MjModel(Class<?>... classes) {
		Arrays.stream(classes).forEach(this::getOrCreateEntity);
	}

	public void addEntity(MjEntity mjEntity) {
		if (!entities.contains(mjEntity)) {
			entities.add(mjEntity);
		}
	}

	public MjEntity getOrCreateEntity(Class<?> clazz) {
		Objects.requireNonNull(clazz);
		Optional<MjEntity> existingEntity = entities.stream().filter(e -> e.getClazz() == clazz).findFirst();
		return existingEntity.orElseGet(() -> new MjEntity(this, clazz));
	}
	
	public MjEntity getEntity(String name) {
		return entities.stream().filter(e -> e.getClassName().equals(name)).findFirst().orElse(null);
	}
	
}