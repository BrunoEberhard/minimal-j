package org.minimalj.repository.sql.view;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;

public class TestEntity {

	public static final TestEntity $ = Keys.of(TestEntity.class);

	public TestEntity() {
		// needed for reflection constructor
	}

	public TestEntity(String name) {
		this.name = name;
	}

	public Object id;

	@Size(30)
	public String name;

	@Searched @Size(61)
	public String getDoubleName() {
		return name + "/" + name;
	}
}
