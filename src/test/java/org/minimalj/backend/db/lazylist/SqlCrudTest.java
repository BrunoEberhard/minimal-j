package org.minimalj.backend.db.lazylist;

import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.persistence.sql.SqlPersistence;
import org.minimalj.util.IdUtils;

import junit.framework.Assert;

public class SqlCrudTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), A.class);
	}
	
	/*
	 * Prepares a simple A containing a B containing a C
	 */
	private A insertAndRead() {
		A a = new A("aName");
		
		B b = new B("bName");
		a.bList = Collections.singletonList(b);
		
		C c = new C("cName");
		b.cList = Collections.singletonList(c);
		
		Object id = persistence.insert(a);
		return persistence.read(A.class, id);
	}

	@Test
	public void testInsertAndRead() {
		A a = insertAndRead();
		Assert.assertEquals("Element with id should be inserted and read", 1, a.bList.size());
		Assert.assertEquals("Element of element should be inserted and read", 1, a.bList.get(0).cList.size());
	}

	@Test
	public void testAddElement() {
		A a = insertAndRead();
		
		a.bList.add(new B("bName2"));

		a = persistence.read(A.class, IdUtils.getId(a));

		Assert.assertEquals("An additional element with id should be persisted when calling add method", 2, a.bList.size());
	}
	
	@Test
	public void testUpdateParent() {
		A a = insertAndRead();
		B b = a.bList.get(0);
		
		C c2 = new C("cName2");
		b.cList = Collections.singletonList(c2);

		persistence.update(b);
		a = persistence.read(A.class, IdUtils.getId(a));

		Assert.assertEquals("Update an element should not remove it from its list", 1, a.bList.size());
		Assert.assertEquals("Update of an element should replace its lists", 1, a.bList.get(0).cList.size());
		Assert.assertEquals("Update of an element should replace its lists", "cName2", a.bList.get(0).cList.get(0).cName);
	}

	@Test
	public void testUseElementTwice() {
		A a1 = insertAndRead();
		A a2 = insertAndRead();

		a1.bList.add(a2.bList.get(0));
		
		a1 = persistence.read(A.class, IdUtils.getId(a1));
		Assert.assertEquals("Add of an element should be possible even if it was used before", 2, a1.bList.size());

		a2 = persistence.read(A.class, IdUtils.getId(a2));
		Assert.assertEquals("The new usage of the element should not change the existing one", 1, a2.bList.size());
	}

}