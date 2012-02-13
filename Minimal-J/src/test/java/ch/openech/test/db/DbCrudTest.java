package ch.openech.test.db;

import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.Table;

public class DbCrudTest {
	
	private static DbPersistence persistence;
	private static Table<A> table;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence();
		table = persistence.addClass(A.class);
		persistence.connect();
	}
	
	@AfterClass
	public static void shutdownDb() throws SQLException {
		persistence.commit();
		persistence.disconnect();
	}
	
	@Test
	public void testSubtable() throws SQLException {
		A a = new A();
		a.aName = "testName1";
		
		B b1 = new B();
		b1.bName = "testNameB1";
		a.b.add(b1);

		B b2 = new B();
		b2.bName = "testNameB2";
		a.b.add(b2);
		
		C c1 = new C();
		c1.cName = "testNameC1";
		a.c.add(c1);

		C c2 = new C();
		c2.cName = "testNameC2";
		a.c.add(c2);

		C c3 = new C();
		c3.cName = "testNameC3";
		a.c.add(c3);

		int id = table.insert(a);
		persistence.commit();
		
		A a2 = table.read(id);
		Assert.assertEquals("testNameB1", a2.b.get(0).bName);
		Assert.assertEquals(2, a2.b.size());
		Assert.assertEquals(3, a2.c.size());
	}
	
	@Test
	public void testSubtableVersion() throws SQLException {
		A a = new A();
		a.aName = "testName1";

		int id = table.insert(a);
		persistence.commit();
		
		A a2 = table.read(id);
		B b1 = new B();
		b1.bName = "testNameB1";
		a2.b.add(b1);
		
		table.update(a2);
		persistence.commit();
		
		List<Integer> versions = table.readVersions(id);
		Assert.assertEquals(1, versions.size());
		
		A a3 = table.read(id, versions.get(0));
		Assert.assertEquals(0, a3.b.size());
		
		A a4 = table.read(id);
		Assert.assertEquals(1, a4.b.size());

		B b2 = new B();
		b2.bName = "testNameB2";
		a4.b.add(b2);
		table.update(a4);
		persistence.commit();

		A a5 = table.read(id);
		a5.b.remove(0);
		table.update(a5);
		persistence.commit();
		
		A a6 = table.read(id);
		a6.b.remove(0);
		table.update(a6);
		persistence.commit();
		
		versions = table.readVersions(id);
		Assert.assertEquals(4, versions.size());

		Assert.assertEquals(0, table.read(id).b.size());
		
		Assert.assertEquals(1, table.read(id, versions.get(3)).b.size());
		Assert.assertEquals(2, table.read(id, versions.get(2)).b.size());
		Assert.assertEquals(1, table.read(id, versions.get(1)).b.size());
		Assert.assertEquals(0, table.read(id, versions.get(0)).b.size());


	}

}
