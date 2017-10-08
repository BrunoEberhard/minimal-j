package org.minimalj.repository.ignite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.ignite.IgniteRepository;

public class IgniteDbOptimisticLockingTest {
	
	private static IgniteRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new IgniteRepository(TestEntity.class);
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
		
		// this tries to update an old version of q
		entity.string = "C";
		repository.update(entity);
	}

	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		public int version;

		@Size(255)
		public String string;
	}
	
}
