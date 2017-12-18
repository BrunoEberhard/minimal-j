package org.minimalj.repository.sql.view;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.query.By;
import org.minimalj.repository.sql.SqlRepository;

public class SqlCrudTest {
	
	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), TestEntity.class);
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