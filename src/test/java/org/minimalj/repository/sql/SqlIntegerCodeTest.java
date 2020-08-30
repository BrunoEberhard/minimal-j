package org.minimalj.repository.sql;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Code;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.DataSourceFactory;

public class SqlIntegerCodeTest {

	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), TestCode.class);
	}
	
	@Test
	public void insertTestCodeWithIntegerId() {
		TestCode code = new TestCode();
		Object id = repository.insert(code);
		Assert.assertEquals("id should be of class Integer", Integer.class, id.getClass());
	}

	public static class TestCode implements Code {
		public Integer id;

		@Size(255)
		public String text;
	}


}
