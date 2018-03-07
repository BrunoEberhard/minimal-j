package org.minimalj.repository.memory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.Repository;

// TODO do this test for all repositories
public class InMemoryDbIdTest {

	private static Repository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new InMemoryRepository(TestEntity.class);
	}
	
	@Test
	public void testStringId() {
		TestEntity entity = new TestEntity();
		entity.text = "Test";
		
		Object id = repository.insert(entity);
		// now convert id to String
		id = id.toString();
		
		entity = repository.read(TestEntity.class, id);
		Assert.assertNotNull("read must work with a String id", entity);
	}

	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		
		@Size(20)
		public String text;
	}

}
