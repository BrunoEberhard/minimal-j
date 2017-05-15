package org.minimalj.repository.memory;

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SqlEnumTest {
	
	private static InMemoryRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new InMemoryRepository(TestEntity.class);
	}
	
	@AfterClass
	public static void shutdownRepository() {
	}
	
	@Test
	public void testCrudDates() {
		TestEntity entity = new TestEntity();
		entity.enuum.add(TestEnum.element2);
		entity.enuum.add(TestEnum.element3);
		
		Object id = repository.insert(entity);

		//
		
		TestEntity entity2 = repository.read(TestEntity.class, id);
		Assert.assertEquals(entity.enuum.size(), entity2.enuum.size());
		Assert.assertFalse(entity2.enuum.contains(TestEnum.element1));
		Assert.assertTrue(entity2.enuum.contains(TestEnum.element2));
		Assert.assertTrue(entity2.enuum.contains(TestEnum.element3));
		
		entity2.enuum.remove(TestEnum.element2);
		repository.update(entity2);
		
		TestEntity entity3 = repository.read(TestEntity.class, id);
		Assert.assertFalse(entity3.enuum.contains(TestEnum.element1));
		Assert.assertFalse(entity3.enuum.contains(TestEnum.element2));
		Assert.assertTrue(entity3.enuum.contains(TestEnum.element3));
	}
	
	public static class TestEntity {
		public Object id;

		public final Set<TestEnum> enuum = new HashSet<>();
	}

	public enum TestEnum {
		element1, element2, element3;
	}
}