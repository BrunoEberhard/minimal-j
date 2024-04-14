package org.minimalj.repository.sql;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Code;
import org.minimalj.model.annotation.AutoIncrement;
import org.minimalj.model.annotation.Size;

public class SqlIntegerCodeTest extends SqlTest {
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { TestCode.class };
	}
	
	@Test
	public void insertTestCodeWithIntegerId() {
		TestCode code = new TestCode();
		Object id = repository.insert(code);
		Assert.assertEquals("id should be of class Integer", Integer.class, id.getClass());
	}

	public static class TestCode implements Code {
		@AutoIncrement
		public Integer id;

		@Size(255)
		public String text;
	}


}
