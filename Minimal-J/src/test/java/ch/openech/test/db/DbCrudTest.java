package ch.openech.test.db;

import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.EmptyObjects;

public class DbCrudTest {
	
	private static DbPersistence persistence;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence(DbPersistence.embeddedDataSource());
		persistence.addHistorizedClass(A.class);
		persistence.createTables();
	}
	
	@AfterClass
	public static void shutdownDb() throws SQLException {
	}
	
	@Test
	public void testCrudWithSubtable() throws SQLException {
		A a = new A("testName1");
		
		a.b.add(new B("testNameB1"));
		a.b.add(new B("testNameB2"));
		
		a.c.add(new C("testNameC1"));
		a.c.add(new C("testNameC2"));
		a.c.add(new C("testNameC3"));

		int id = persistence.insert(a);

		//
		
		A a2 = persistence.read(A.class, id);
		Assert.assertEquals("The string in the first B of A should match the original String", "testNameB1", a2.b.get(0).bName);
		Assert.assertEquals("The string in the second C of A should match the original String", "testNameC2", a2.c.get(1).cName);
		Assert.assertEquals("The count of the B's attached to A should match", 2, a2.b.size());
		Assert.assertEquals("The count of the C's attached to A should match", 3, a2.c.size());
	}
	
	@Test
	public void testSubtableVersion() throws SQLException {
		int id = writeSimpleA();
		readTheAandAddBandE(id);
		
		List<Integer> versions = persistence.readVersions(A.class, id);
		Assert.assertEquals("A should now have 1 historized version", 1, versions.size());
		
		A a3 = persistence.read(A.class, id, versions.get(0));
		Assert.assertEquals("The historized (first) version of A should not have any B attached", 0, a3.b.size());
		Assert.assertTrue("The historized (first) version of A should not have a E attached", EmptyObjects.isEmpty(a3.e));
		
		A a4 = persistence.read(A.class, id);
		Assert.assertEquals("The actual version of A should have a B attached", 1, a4.b.size());
		Assert.assertNotNull("The actual version of A should have a E attached", a4.e);

		addAnotherB(a4);
		removeFirstB(id);
		removeFirstB(id);
		
		versions = persistence.readVersions(A.class, id);
		Assert.assertEquals("A should now have 4 historized versions", 4, versions.size());

		Assert.assertEquals("Every B should be removed from the A now", 0, persistence.read(A.class, id).b.size());
		
		// now check for the right amount of B's attached to A in every version
		Assert.assertEquals(1, persistence.read(A.class, id, versions.get(3)).b.size());
		Assert.assertEquals(2, persistence.read(A.class, id, versions.get(2)).b.size());
		Assert.assertEquals(1, persistence.read(A.class, id, versions.get(1)).b.size());
		Assert.assertEquals(0, persistence.read(A.class, id, versions.get(0)).b.size());
	}

	private int writeSimpleA() throws SQLException {
		A a = new A("testName1");

		int id = persistence.insert(a);
		return id;
	}

	private void readTheAandAddBandE(int id) throws SQLException {
		A a2 = persistence.read(A.class, id);
		a2.b.add(new B("testNameB1"));
		a2.e = new E();
		a2.e.e = "AddedE";
		
		persistence.update(a2);
	}

	private void addAnotherB(final A a4) throws SQLException {
		a4.b.add(new B("testNameB2"));
		persistence.update(a4);
	}

	private void removeFirstB(final int id) throws SQLException {
		A a5 = persistence.read(A.class, id);
		a5.b.remove(0);
		persistence.update(a5);
	}

}
