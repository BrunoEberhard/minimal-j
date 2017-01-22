package org.minimalj.repository.sql;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;

public class SqlSelfReferenceTest {

	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), TestEntity.class);
	}

	@Test
	public void testSelfReferencingEntity() {
		TestEntity e = new TestEntity();
		e.reference = e;
		Object id = repository.insert(e);
		e = repository.read(TestEntity.class, id);
		Assert.assertEquals(e, e.reference);
	}
	
	@Test @Ignore // not yet solved
	public void testCycleTest() {
		TestEntity e1 = new TestEntity();
		TestEntity e2 = new TestEntity();
		e1.reference = e2;
		e2.reference = e1;
		Object id = repository.insert(e1);
		e1 = repository.read(TestEntity.class, id);
		Assert.assertEquals(e1, e1.reference.reference);
	}
	
	
	public static class TestEntity {
		public Object id;
		
		public TestEntity reference;
		
	}
	
}
