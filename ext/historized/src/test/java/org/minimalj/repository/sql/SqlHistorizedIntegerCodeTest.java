package org.minimalj.repository.sql;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Code;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.DataSourceFactory;

public class SqlHistorizedIntegerCodeTest {

	private static SqlHistorizedRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlHistorizedRepository(DataSourceFactory.embeddedDataSource(), TestCode.class);
	}
	
	@Test
	public void insertTestCodeWithIntegerId() {
		TestCode code = new TestCode();
		code.id = 1;
		code.text = "Phase 1";
		Object id = repository.insert(code);
		Assert.assertEquals("id should be of class Integer", Integer.class, id.getClass());
		
		code = repository.read(TestCode.class, id);
		code.text = "Phase 2";
		repository.update(code);
		
		TestCode codeNew = repository.read(TestCode.class, id);
		Assert.assertTrue("version should be increased", codeNew.version > code.version);

		TestCode codeOld = repository.readVersion(TestCode.class, id, code.version);
		Assert.assertTrue("old version should be historized", codeOld.historized);
	}

	public static class TestCode implements Code {
		public Integer id;
		public boolean historized;
		public int version;
		
		@Size(255)
		public String text;
	}

}
