package org.minimalj.backend.db.lazylist;

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.sql.SqlPersistence;

import junit.framework.Assert;

public class SqlCrudTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), A.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testInsertAndDelete() {
		A a = new A();
		a.aName = "aName";
		B b = new B();
		b.bName = "bName";
		a.b = Collections.singletonList(b);
		C c = new C();
		c.cName = "cName";
		b.c = Collections.singletonList(c);
		
		Object id = persistence.insert(a);

		a = persistence.read(A.class, id);
		
		Assert.assertEquals(1, a.b.size());
		Assert.assertEquals(1, a.b.get(0).c.size());
	}
	
}