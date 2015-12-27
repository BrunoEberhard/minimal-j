package org.minimalj.backend.db.viewlist;

import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.sql.SqlPersistence;

public class SqlViewListTest {
	
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
		
		persistence.insert(a);
	}
	
	@Test 
	public void testInsertWithExistingReferencedObject() {
		B b = new B("B1");
		Object bId = persistence.insert(b);
		b = persistence.read(B.class, bId);
		
		A a = new A("A1");
		a.b = Collections.singletonList(b);
		persistence.insert(a);
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
		
		a2.b.get(0).c.get(0);
	}

}
