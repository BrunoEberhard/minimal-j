package org.minimalj.repository.sql;

import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.DataSourceFactory;

public class SqlHistorizedOptimisticLockingTest {
	
	private static SqlHistorizedRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlHistorizedRepository(DataSourceFactory.embeddedDataSource(), TestEntityHistorized.class);
	}
	
	@Test
	public void testHistorizedOptimisticLockingOk() {
		TestEntityHistorized entity = new TestEntityHistorized();
		entity.string = "A";
		Object id = repository.insert(entity);
		entity = repository.read(TestEntityHistorized.class, id);
		
		entity.string = "B";
		repository.update(entity);
		entity = repository.read(TestEntityHistorized.class, id);
		
		entity.string = "C";
		repository.update(entity);
	}

	@Test(expected = Exception.class)
	public void testHistorizedOptimisticLockingFail() {
		TestEntityHistorized entity = new TestEntityHistorized();
		entity.string = "A";
		Object id = repository.insert(entity);
		entity = repository.read(TestEntityHistorized.class, id);
		
		entity.string = "B";
		repository.update(entity);
		// here the read is forgotten
		
		// this tries to update an old version of r
		entity.string = "C";
		repository.update(entity);
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
