package org.minimalj.metamodel.generator;

import org.minimalj.metamodel.model.MjEntity;

public class GeneratorEntity extends MjEntity {

	public String name;
	public String packageName;

	public GeneratorEntity() {
	}

	public GeneratorEntity(String name) {
		this.name = name;
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

}
