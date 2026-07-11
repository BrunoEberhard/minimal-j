package org.minimalj.repository.sql;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.repository.query.By;

public class SqlPrimitiveTest extends SqlTest {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { TestEntity.class };
	}

	@Test
	public void testCrud() {
		TestEntity entity = new TestEntity();
		entity.intField = 42;
		entity.longField = 43;

		Object id = repository.insert(entity);

		TestEntity read = repository.read(TestEntity.class, id);
		Assert.assertEquals(42, read.intField);
		Assert.assertEquals(43L, read.longField);

		read.intField = 44;
		read.longField = 45;
		repository.update(read);

		read = repository.read(TestEntity.class, id);
		Assert.assertEquals(44, read.intField);
		Assert.assertEquals(45L, read.longField);
	}

	@Test
	public void testDefaultValues() {
		TestEntity entity = new TestEntity();

		Object id = repository.insert(entity);

		TestEntity read = repository.read(TestEntity.class, id);
		Assert.assertEquals(0, read.intField);
		Assert.assertEquals(0L, read.longField);
	}

	@Test
	public void testFindByPrimitiveField() {
		TestEntity entity = new TestEntity();
		entity.intField = 46;
		entity.longField = 47;
		repository.insert(entity);

		List<TestEntity> list = repository.find(TestEntity.class, By.field(TestEntity.$.intField, 46));
		Assert.assertEquals("Entity should be found by the value of its int field", 1, list.size());

		list = repository.find(TestEntity.class, By.field(TestEntity.$.longField, 47L));
		Assert.assertEquals("Entity should be found by the value of its long field", 1, list.size());
	}

	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);

		public Object id;

		public int intField;
		public long longField;
	}
}
