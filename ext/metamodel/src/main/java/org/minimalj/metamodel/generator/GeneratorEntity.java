package org.minimalj.metamodel.generator;

import org.minimalj.metamodel.model.MjEntity;

public class GeneratorEntity extends MjEntity {

	public String name;
	public String packageName;
	public Boolean noId = false;
	
	public GeneratorEntity() {
		this.type = MjEntityType.ENTITY;
	}

	public GeneratorEntity(String name) {
		this.name = name;
		this.type = MjEntityType.ENTITY;
	}

	public GeneratorEntity(MjEntityType type) {
		super(type);
	}

	@Override
	public String getClassName() {
		return name;
	}

	@Override
	public String getPackageName() {
		return packageName;
	}
	
	@Override
	public boolean isPrimitiv() {
		return false;
	}
	
}
