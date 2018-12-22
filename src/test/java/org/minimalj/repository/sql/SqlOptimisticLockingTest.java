package org.minimalj.repository.sql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.DataSourceFactory;

public class SqlOptimisticLockingTest {
	
	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), TestEntity.class);
	}
	
	@AfterClass
	public static void shutdownRepository() {
	}
	
	@Test
	public void testOptimisticLockingOk() {
		TestEntity entity = new TestEntity();
		entity.string = "A";
		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);
		
		entity.string = "B";
		repository.update(entity);
		entity = repository.read(TestEntity.class, id);
		
		entity.string = "C";
		repository.update(entity);
	}

	@Test(expected = Exception.class)
	public void testOptimisticLockingFail() {
		TestEntity entity = new TestEntity();
		entity.string = "A";
		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);
		
		entity.string = "B";
		repository.update(entity);
		// here the read is forgotten
		
		// this tries to update an old version
		entity.string = "C";
		repository.update(entity);
	}

	@Test(expected = Exception.class)
	public void testOptimisticLockingFailDependingEntity() {
		TestEntity entity = new TestEntity();
		entity.b = new B("step1");
		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);

		entity.b.bName = "step2a";
		repository.update(entity);
		// here the read is forgotten

		// this tries to update an old version
		entity.b.bName = "step2b";
		repository.update(entity);
	}

	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		public int version;

		@Size(255)
		public String string;

		// depending (without id)
		public B b;
	}
	
}
