package org.minimalj.persistence.sql;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class SqlLongFieldNameTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), TestEntity.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testInsertAndDelete() {
		TestEntity entity = new TestEntity();
		Object id = persistence.insert(entity);
		
		TestEntity l2 = persistence.read(TestEntity.class, id);
		Assert.assertNotNull(l2);
		
		persistence.delete(l2);
		
		TestEntity l3 = persistence.read(TestEntity.class, id);
		Assert.assertNull(l3);
	}

	public static class TestEntity {

		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		public int version;
		public boolean historized;

		@Size(30)
		public String aVeryLongFieldNameAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyz;

		@Size(30)
		public String aVeryLongFieldNameAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyz2;

	}

}
