package org.minimalj.repository.sql;

import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;

public class SqlBooleanTest extends SqlTest {
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { TestEntity.class };
	}

	@Test
	public void testValidBooleans() {
		TestEntity entity = new TestEntity();
		entity.notEmptyBoolean = true;
		entity.optionalBoolean = false;
		
		repository.insert(entity);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidBooleans() {
		TestEntity entity = new TestEntity();
		
		SqlCrudTest.executeWithoutLog(() -> repository.insert(entity));
	}
	
	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		
		@NotEmpty
		public Boolean notEmptyBoolean;
		
		public Boolean optionalBoolean;
	}


}
