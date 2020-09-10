package org.minimalj.repository.sql.view;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.repository.query.By;
import org.minimalj.repository.sql.SqlTest;

public class SqlCrudTest extends SqlTest {
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { TestEntity.class };
	}

	@Override
	protected void initData() {
		TestEntity entity = new TestEntity("aName");
		repository.insert(entity);
	}
	
	@Test
	public void testRead() {
		TestEntity entity = repository.find(TestEntity.class, By.search("N")).get(0);
		Assert.assertEquals("aName", entity.name);
		Assert.assertEquals("aName/aName", entity.getDoubleName());
	}

	@Test
	public void testReadView() {
		TestView view = repository.find(TestView.class, By.search("e/a")).get(0);
		
		Assert.assertEquals("aName", view.name);
		Assert.assertEquals("aName/aName", view.doubleName);
	}
	
}