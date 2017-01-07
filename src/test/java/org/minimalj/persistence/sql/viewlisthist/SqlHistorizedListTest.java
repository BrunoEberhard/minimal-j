package org.minimalj.persistence.sql.viewlisthist;

import java.util.Collections;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.minimalj.persistence.sql.SqlPersistence;

@Ignore // HistorizedLazyListTable not yet implemented
public class SqlHistorizedListTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), TestEntity.class, TestElementB.class, TestElementC.class);
	}
	
	@Test 
	public void testInsertWithNewReferencedObject() {
		TestEntity entity = new TestEntity("A1");
		
		TestElementB b = new TestElementB("B1");
		entity.list = Collections.singletonList(b);
		
		TestElementC c = new TestElementC("C1");
		b.list = Collections.singletonList(c);
		
		Object entityId = persistence.insert(entity);
		
		TestEntity entity2 = persistence.read(TestEntity.class, entityId);
		Assert.assertEquals("B1", entity2.list.get(0).name);
		
		// lists of c is not loaded
	}
	
	@Test 
	public void testInsertWithExistingReferencedObject() {
		TestElementB b = new TestElementB("B1");
		Object bId = persistence.insert(b);
		b = persistence.read(TestElementB.class, bId);
		
		TestEntity entity = new TestEntity("A1");
		entity.list = Collections.singletonList(b);
		
		Object id = persistence.insert(entity);
		
		TestEntity a2 = persistence.read(TestEntity.class, id);
		Assert.assertEquals("B1", a2.list.get(0).name);
	}
	
	@Test(expected = RuntimeException.class)
	public void testUnloadedList() {
		TestEntity entity = new TestEntity("A1");
		
		TestElementB b = new TestElementB("B1");
		entity.list = Collections.singletonList(b);
		
		TestElementC c = new TestElementC("C1");
		b.list = Collections.singletonList(c);
		
		Object id = persistence.insert(entity);
		
		TestEntity entity2 = persistence.read(TestEntity.class, id);
		
		Assert.assertEquals(1, entity2.list.size());
		
		entity2.list.get(0).list.get(0);
	}

}
