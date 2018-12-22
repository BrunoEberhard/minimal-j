package org.minimalj.repository.sql.relationhist;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.sql.SqlHistorizedRepository;
import org.minimalj.repository.sql.SqlRepository;

public class SqlHistorizedListTest {
	
	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlHistorizedRepository(DataSourceFactory.embeddedDataSource(), TestEntity.class, TestElementHistorized.class, TestElementC.class);
	}
	
	@Test 
	public void testInsertWithNewReferencedObject() {
		TestEntity entity = new TestEntity("A1");
		
		TestElementHistorized b = new TestElementHistorized("B1");
		entity.list = Collections.singletonList(b);
		
		TestElementC c = new TestElementC("C1");
		b.list = Collections.singletonList(c);
		
		Object entityId = repository.insert(entity);
		
		TestEntity entity2 = repository.read(TestEntity.class, entityId);
		Assert.assertEquals("B1", entity2.list.get(0).name);
		
		// lists of c is not loaded
	}
	
	@Test 
	public void testInsertWithExistingReferencedObject() {
		TestElementHistorized b = new TestElementHistorized("B1");
		Object bId = repository.insert(b);
		b = repository.read(TestElementHistorized.class, bId);
		
		TestEntity entity = new TestEntity("A1");
		entity.list = Collections.singletonList(b);
		
		Object id = repository.insert(entity);
		
		TestEntity a2 = repository.read(TestEntity.class, id);
		Assert.assertEquals("B1", a2.list.get(0).name);
	}
	
	@Test
	public void testHistory() {
		TestElementHistorized b1 = new TestElementHistorized("B1");
		b1 = repository.read(TestElementHistorized.class, repository.insert(b1));

		TestElementHistorized b2 = new TestElementHistorized("B2");
		b2 = repository.read(TestElementHistorized.class, repository.insert(b2));

		TestElementHistorized b3 = new TestElementHistorized("B3");
		b3 = repository.read(TestElementHistorized.class, repository.insert(b3));

		TestEntity entity = new TestEntity("A");
		entity.list = Arrays.asList(b1, b2, b3);

		Object id = repository.insert(entity);
		TestEntity step1 = repository.read(TestEntity.class, id);
		Assert.assertEquals("B1", step1.list.get(0).name);
		Assert.assertEquals("B2", step1.list.get(1).name);
		Assert.assertEquals("B3", step1.list.get(2).name);

		// insert new element at end

		TestElementHistorized b4 = new TestElementHistorized("B4");
		b4 = repository.read(TestElementHistorized.class, repository.insert(b4));

		step1.list.add(b4);
		repository.update(step1);
		TestEntity step2 = repository.read(TestEntity.class, id);
		Assert.assertEquals("B1", step2.list.get(0).name);
		Assert.assertEquals("B2", step2.list.get(1).name);
		Assert.assertEquals("B3", step2.list.get(2).name);
		Assert.assertEquals("B4", step2.list.get(3).name);

		// remove element

		step2.list.remove(2);
		repository.update(step2);
		TestEntity step3 = repository.read(TestEntity.class, id);
		Assert.assertEquals("B1", step3.list.get(0).name);
		Assert.assertEquals("B2", step3.list.get(1).name);
		Assert.assertEquals("B4", step3.list.get(2).name);

		// insert new element at beginning

		TestElementHistorized b5 = new TestElementHistorized("B5");
		b5 = repository.read(TestElementHistorized.class, repository.insert(b5));

		step3.list.add(0, b5);
		repository.update(step3);
		TestEntity step4 = repository.read(TestEntity.class, id);
		Assert.assertEquals("B5", step4.list.get(0).name);
		Assert.assertEquals("B1", step4.list.get(1).name);
		Assert.assertEquals("B2", step4.list.get(2).name);
		Assert.assertEquals("B4", step4.list.get(3).name);
	}

	@Test
	public void testElementHistory() {
		TestElementHistorized b1 = new TestElementHistorized("B");
		Object elementId = repository.insert(b1);
		b1 = repository.read(TestElementHistorized.class, elementId);

		TestEntity entity = new TestEntity("A");
		entity.list = Arrays.asList(b1);

		Object id = repository.insert(entity);
		TestEntity step1 = repository.read(TestEntity.class, id);
		Assert.assertEquals("B", step1.list.get(0).name);

		// change element

		b1.name = "B - new";
		repository.update(b1);

		TestEntity step2 = repository.read(TestEntity.class, id);
		Assert.assertEquals("The right version of the element should be used", "B", step2.list.get(0).name);

		// delete element

		repository.delete(b1);
		Assert.assertNull(repository.read(TestElementHistorized.class, elementId));

		TestEntity step3 = repository.read(TestEntity.class, id);
		Assert.assertEquals("After deletion an earlier version should be kept as element", "B", step2.list.get(0).name);
	}
}
