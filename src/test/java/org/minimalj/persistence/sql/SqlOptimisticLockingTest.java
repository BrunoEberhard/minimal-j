package org.minimalj.persistence.sql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class SqlOptimisticLockingTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), TestEntity.class, TestEntityHistorized.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testOptimisticLockingOk() {
		TestEntity entity = new TestEntity();
		entity.string = "A";
		Object id = persistence.insert(entity);
		entity = persistence.read(TestEntity.class, id);
		
		entity.string = "B";
		persistence.update(entity);
		entity = persistence.read(TestEntity.class, id);
		
		entity.string = "C";
		persistence.update(entity);
	}

	@Test(expected = Exception.class)
	public void testOptimisticLockingFail() {
		TestEntity entity = new TestEntity();
		entity.string = "A";
		Object id = persistence.insert(entity);
		entity = persistence.read(TestEntity.class, id);
		
		entity.string = "B";
		persistence.update(entity);
		// here the read is forgotten
		
		// this tries to update an old version of q
		entity.string = "C";
		persistence.update(entity);
	}
	
	@Test
	public void testHistorizedOptimisticLockingOk() {
		TestEntityHistorized entity = new TestEntityHistorized();
		entity.string = "A";
		Object id = persistence.insert(entity);
		entity = persistence.read(TestEntityHistorized.class, id);
		
		entity.string = "B";
		persistence.update(entity);
		entity = persistence.read(TestEntityHistorized.class, id);
		
		entity.string = "C";
		persistence.update(entity);
	}

	@Test(expected = Exception.class)
	public void testHistorizedOptimisticLockingFail() {
		TestEntityHistorized entity = new TestEntityHistorized();
		entity.string = "A";
		Object id = persistence.insert(entity);
		entity = persistence.read(TestEntityHistorized.class, id);
		
		entity.string = "B";
		persistence.update(entity);
		// here the read is forgotten
		
		// this tries to update an old version of r
		entity.string = "C";
		persistence.update(entity);
	}

	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		public int version;

		@Size(255)
		public String string;
	}
	
	public static class TestEntityHistorized {
		public static final TestEntityHistorized $ = Keys.of(TestEntityHistorized.class);
		
		public Object id;
		public int version;
		public boolean historized;

		@Size(255)
		public String string;
	}

}
