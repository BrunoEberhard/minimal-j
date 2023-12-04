package org.minimalj.repository.sql;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;

public class SqlQueryTransientFieldTest extends SqlTest {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { TestClass.class };
	}

	@Test
	public void testQuery() {
		TestClass testObject = new TestClass();
		testObject.a = 2;
		repository.insert(testObject);

		testObject = repository.execute(TestClass.class, "SELECT t.*, " + $(TestClass.$.a) + " * 3 AS "
				+ $(TestClass.$.b) + " FROM " + $(TestClass.class) + " t");
		Assert.assertNotNull(testObject);
		Assert.assertNotNull("Transient fields should be filled by a query containing these fields", testObject.b);
		Assert.assertEquals(6, testObject.b.longValue());
	}

	private String $(Object classOrKey) {
		return repository.name(classOrKey);
	}

	public static class TestClass {
		public static final TestClass $ = Keys.of(TestClass.class);

		public Object id;

		public Integer a;

		public transient Integer b;
	}

}
