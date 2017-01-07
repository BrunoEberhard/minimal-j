package org.minimalj.persistence.sql;

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SqlEnumTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), TestEntity.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testCrudDates() {
		TestEntity entity = new TestEntity();
		entity.enuum.add(TestEnum.element2);
		entity.enuum.add(TestEnum.element3);
		
		Object id = persistence.insert(entity);

		//
		
		TestEntity entity2 = persistence.read(TestEntity.class, id);
		Assert.assertEquals(entity.enuum.size(), entity2.enuum.size());
		Assert.assertFalse(entity2.enuum.contains(TestEnum.element1));
		Assert.assertTrue(entity2.enuum.contains(TestEnum.element2));
		Assert.assertTrue(entity2.enuum.contains(TestEnum.element3));
		
		entity2.enuum.remove(TestEnum.element2);
		persistence.update(entity2);
		
		TestEntity entity3 = persistence.read(TestEntity.class, id);
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