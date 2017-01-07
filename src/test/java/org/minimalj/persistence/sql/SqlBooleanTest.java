package org.minimalj.persistence.sql;

import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;

public class SqlBooleanTest {

	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), TestEntity.class);
	}
	
	@Test
	public void testValidBooleans() {
		TestEntity entity = new TestEntity();
		entity.notEmptyBoolean = true;
		entity.optionalBoolean = false;
		
		persistence.insert(entity);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidBooleans() {
		TestEntity entity = new TestEntity();
		
		persistence.insert(entity);
	}
	
	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		
		@NotEmpty
		public Boolean notEmptyBoolean;
		
		public Boolean optionalBoolean;
	}


}
