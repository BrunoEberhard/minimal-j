package org.minimalj.backend.db;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.db.DbPersistence;

public class DbEnumTest {
	
	private static DbPersistence persistence;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence(DbPersistence.embeddedDataSource(), F.class);
	}
	
	@AfterClass
	public static void shutdownDb() throws SQLException {
	}
	
	@Test
	public void testCrudDates() throws SQLException {
		F f = new F();
		f.fenum.add(FEnum.element2);
		f.fenum.add(FEnum.element3);
		
		Object id = persistence.insert(f);

		//
		
		F f2 = persistence.read(F.class, id);
		Assert.assertEquals(f.fenum.size(), f2.fenum.size());
		Assert.assertFalse(f2.fenum.contains(FEnum.element1));
		Assert.assertTrue(f2.fenum.contains(FEnum.element2));
		Assert.assertTrue(f2.fenum.contains(FEnum.element3));
		
		f2.fenum.remove(FEnum.element2);
		persistence.update(f2);
		
		F f3 = persistence.read(F.class, id);
		Assert.assertFalse(f3.fenum.contains(FEnum.element1));
		Assert.assertFalse(f3.fenum.contains(FEnum.element2));
		Assert.assertTrue(f3.fenum.contains(FEnum.element3));
	}

}