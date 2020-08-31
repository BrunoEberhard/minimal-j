package org.minimalj.repository.sql;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class SqlLongFieldNameTest extends SqlTest {
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { TestEntity.class };
	}
	
	@Test
	public void testInsertAndDelete() {
		TestEntity entity = new TestEntity();
		Object id = repository.insert(entity);
		
		TestEntity l2 = repository.read(TestEntity.class, id);
		Assert.assertNotNull(l2);
		
		repository.delete(l2);
		
		TestEntity l3 = repository.read(TestEntity.class, id);
		Assert.assertNull(l3);
	}

	public static class TestEntity {

		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		public int version;

		@Size(30)
		public String aVeryLongFieldNameAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyz;

		@Size(30)
		public String aVeryLongFieldNameAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyz2;

	}

}
