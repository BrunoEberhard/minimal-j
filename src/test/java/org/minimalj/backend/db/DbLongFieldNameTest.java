package org.minimalj.backend.db;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DbLongFieldNameTest {
	
	private static DbPersistence persistence;
	
	@BeforeClass
	public static void setupDb() {
		persistence = new DbPersistence(DbPersistence.embeddedDataSource(), L.class);
	}
	
	@AfterClass
	public static void shutdownDb() {
	}
	
	@Test
	public void testInsertAndDelete() {
		L l = new L();
		Object id = persistence.insert(l);
		
		L l2 = persistence.read(L.class, id);
		Assert.assertNotNull(l2);
		
		persistence.delete(l2);
		
		L l3 = persistence.read(L.class, id);
		Assert.assertNull(l3);
	}

}
