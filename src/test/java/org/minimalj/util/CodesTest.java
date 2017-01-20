package org.minimalj.util;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Code;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.criteria.By;
import org.minimalj.repository.sql.SqlRepository;

public class CodesTest {

	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), TestCode.class);
	}
	
	@Test
	public void testDeclaratedCodes() {
		List<TestCode> codes = repository.read(TestCode.class, By.all(), 3);
		Assert.assertEquals("public static final codes should be automatically inserted in the repository", 2, codes.size());
	}

	@Test
	public void testDeclaratedCode() {
		TestCode code = repository.read(TestCode.class, TestCode.A.id);
		Assert.assertEquals(TestCode.A.text, code.text);
	}
	
	public static class TestCode implements Code {
		@Size(2)
		public String id;
		@Size(255)
		public String text;
		
		public static final TestCode A = new TestCode("42", "Text42");
		public static final TestCode B = new TestCode("43", "Text43");

		// not final declarations must not generate an entry in database
		public static TestCode NOT_FINAL = new TestCode("44", "Text44");

		public TestCode() {
		}
		
		public TestCode(String id, String text) {
			this.id = id;
			this.text = text;
		}
	}
}
