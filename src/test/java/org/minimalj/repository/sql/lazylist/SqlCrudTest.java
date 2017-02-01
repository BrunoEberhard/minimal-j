package org.minimalj.repository.sql.lazylist;

import java.util.Collections;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.sql.SqlRepository;
import org.minimalj.util.IdUtils;

public class SqlCrudTest {
	
	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), TestEntity.class);
	}
	
	/*
	 * Prepares a simple A containing a B containing a C
	 */
	private TestEntity insertAndRead() {
		TestEntity entity = new TestEntity("aName");
		
		TestElementB b = new TestElementB("bName");
		entity.list = Collections.singletonList(b);
		
		TestElementC c = new TestElementC("cName");
		b.list = Collections.singletonList(c);
		
		Object id = repository.insert(entity);
		return repository.read(TestEntity.class, id);
	}

	@Test
	public void testInsertAndRead() {
		TestEntity entity = insertAndRead();
		Assert.assertEquals("Element with id should be inserted and read", 1, entity.list.size());
		Assert.assertEquals("Element of element should be inserted and read", 1, entity.list.get(0).list.size());
	}

	@Test
	public void testAddElement() {
		TestEntity entity = insertAndRead();
		
		entity.list.add(new TestElementB("bName2"));
		repository.update(entity);
		
		entity = repository.read(TestEntity.class, IdUtils.getId(entity));

		Assert.assertEquals("An additional element with id should be persisted when calling add method", 2, entity.list.size());
	}
	
	@Test
	public void testUpdateParent() {
		TestEntity entity = insertAndRead();
		TestElementB b = entity.list.get(0);
		
		TestElementC c2 = new TestElementC("cName2");
		b.list = Collections.singletonList(c2);

		repository.update(b);
		entity = repository.read(TestEntity.class, IdUtils.getId(entity));

		Assert.assertEquals("Update an element should not remove it from its list", 1, entity.list.size());
		Assert.assertEquals("Update of an element should replace its lists", 1, entity.list.get(0).list.size());
		Assert.assertEquals("Update of an element should replace its lists", "cName2", entity.list.get(0).list.get(0).name);
	}

	@Test
	public void testUseElementTwice() {
		TestEntity entity1 = insertAndRead();
		TestEntity entity2 = insertAndRead();

		entity1.list.add(entity2.list.get(0));
		repository.update(entity1);
		
		entity1 = repository.read(TestEntity.class, IdUtils.getId(entity1));
		Assert.assertEquals("Add of an element should be possible even if it was used before", 2, entity1.list.size());

		entity2 = repository.read(TestEntity.class, IdUtils.getId(entity2));
		Assert.assertEquals("The new usage of the element should not change the existing one", 1, entity2.list.size());
	}

	@Test
	public void testNullList() {
		TestEntity entity = new TestEntity("aName");
		
		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);
		
		Assert.assertTrue(entity.list == null || entity.list.isEmpty());
	}

}