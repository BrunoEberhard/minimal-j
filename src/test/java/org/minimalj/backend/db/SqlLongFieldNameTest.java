package org.minimalj.backend.db;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.persistence.sql.SqlPersistence;

public class SqlLongFieldNameTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), L.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
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
