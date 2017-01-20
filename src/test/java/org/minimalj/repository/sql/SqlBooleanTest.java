package org.minimalj.repository.sql;

import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.repository.Repository;
import org.minimalj.repository.DataSourceFactory;

public class SqlBooleanTest {

	private static Repository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), TestEntity.class);
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
		
		repository.insert(entity);
	}
	
	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		
		@NotEmpty
		public Boolean notEmptyBoolean;
		
		public Boolean optionalBoolean;
	}


}
