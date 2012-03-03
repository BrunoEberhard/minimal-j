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
	public void testCrudWithSubtable() throws SQLException {
		A a = new A("testName1");
		
		a.b.add(new B("testNameB1"));
		a.b.add(new B("testNameB2"));
		
		a.c.add(new C("testNameC1"));
		a.c.add(new C("testNameC2"));
		a.c.add(new C("testNameC3"));

		int id = table.insert(a);
		persistence.commit();

		//
		
		A a2 = table.read(id);
		Assert.assertEquals("The string in the first B of A should match the original String", "testNameB1", a2.b.get(0).bName);
		Assert.assertEquals("The string in the second C of A should match the original String", "testNameC2", a2.c.get(1).cName);
		Assert.assertEquals("The count of the B's attached to A should match", 2, a2.b.size());
		Assert.assertEquals("The count of the C's attached to A should match", 3, a2.c.size());
	}
	
	@Test
	public void testSubtableVersion() throws SQLException {
		int id = writeSimpleA();
		readTheAandAddB(id);
		
		List<Integer> versions = table.readVersions(id);
		Assert.assertEquals("A should now have 1 historized version", 1, versions.size());
		
		A a3 = table.read(id, versions.get(0));
		Assert.assertEquals("The historized (first) version of A should not have any B attached", 0, a3.b.size());
		
		A a4 = table.read(id);
		Assert.assertEquals("The actual version of A should have a B attached", 1, a4.b.size());

		addAnotherB(a4);
		removeFirstB(id);
		removeFirstB(id);
		
		versions = table.readVersions(id);
		Assert.assertEquals("A should now have 4 historized versions", 4, versions.size());

		Assert.assertEquals("Every B should be removed from the A now", 0, table.read(id).b.size());
		
		// now check for the right amount of B's attached to A in every version
		Assert.assertEquals(1, table.read(id, versions.get(3)).b.size());
		Assert.assertEquals(2, table.read(id, versions.get(2)).b.size());
		Assert.assertEquals(1, table.read(id, versions.get(1)).b.size());
		Assert.assertEquals(0, table.read(id, versions.get(0)).b.size());
	}

	private int writeSimpleA() throws SQLException {
		A a = new A("testName1");

		int id = table.insert(a);
		persistence.commit();
		return id;
	}

	private void readTheAandAddB(int id) throws SQLException {
		A a2 = table.read(id);
		a2.b.add(new B("testNameB1"));
		
		table.update(a2);
		persistence.commit();
	}

	private void addAnotherB(A a4) throws SQLException {
		a4.b.add(new B("testNameB2"));
		table.update(a4);
		persistence.commit();
	}

	private void removeFirstB(int id) throws SQLException {
		A a5 = table.read(id);
		a5.b.remove(0);
		table.update(a5);
		persistence.commit();
	}

}
