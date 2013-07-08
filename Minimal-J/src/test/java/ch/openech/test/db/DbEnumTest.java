package ch.openech.test.db;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.Table;

public class DbEnumTest {
	
	private static DbPersistence persistence;
	private static Table<F> table;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence();
		table = persistence.addClass(F.class);
		persistence.connect();
	}
	
	@AfterClass
	public static void shutdownDb() throws SQLException {
		persistence.commit();
		persistence.disconnect();
	}
	
	@Test
	public void testCrudDates() throws SQLException {
		F f = new F();
		f.fenum.add(FEnum.element2);
		f.fenum.add(FEnum.element3);
		
		int id = table.insert(f);
		persistence.commit();

		//
		
		F f2 = table.read(id);
		Assert.assertEquals(f.fenum.size(), f2.fenum.size());
		Assert.assertFalse(f2.fenum.contains(FEnum.element1));
		Assert.assertTrue(f2.fenum.contains(FEnum.element2));
		Assert.assertTrue(f2.fenum.contains(FEnum.element3));
		
		f2.fenum.remove(FEnum.element2);
		table.update(f2);
		persistence.commit();
		
		F f3 = table.read(id);
		Assert.assertFalse(f3.fenum.contains(FEnum.element1));
		Assert.assertFalse(f3.fenum.contains(FEnum.element2));
		Assert.assertTrue(f3.fenum.contains(FEnum.element3));
	}

}