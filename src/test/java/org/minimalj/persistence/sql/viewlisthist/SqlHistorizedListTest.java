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
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), A.class, B.class, C.class);
	}
	
	@Test 
	public void testInsertWithNewReferencedObject() {
		A a = new A("A1");
		
		B b = new B("B1");
		a.b = Collections.singletonList(b);
		
		C c = new C("C1");
		b.c = Collections.singletonList(c);
		
		Object aId = persistence.insert(a);
		
		A a2 = persistence.read(A.class, aId);
		Assert.assertEquals("B1", a2.b.get(0).bName);
		
		// lists of c is not loaded
	}
	
	@Test 
	public void testInsertWithExistingReferencedObject() {
		B b = new B("B1");
		Object bId = persistence.insert(b);
		b = persistence.read(B.class, bId);
		
		A a = new A("A1");
		a.b = Collections.singletonList(b);
		
		Object aId = persistence.insert(a);
		
		A a2 = persistence.read(A.class, aId);
		Assert.assertEquals("B1", a2.b.get(0).bName);
	}
	
	@Test(expected = RuntimeException.class)
	public void testUnloadedList() {
		A a = new A("A1");
		
		B b = new B("B1");
		a.b = Collections.singletonList(b);
		
		C c = new C("C1");
		b.c = Collections.singletonList(c);
		
		Object aId = persistence.insert(a);
		
		A a2 = persistence.read(A.class, aId);
		
		Assert.assertEquals(1, a2.b.size());
		
		a2.b.get(0).c.get(0);
	}

}
