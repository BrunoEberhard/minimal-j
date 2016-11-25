package org.minimalj.persistence.sql;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.persistence.sql.EmptyObjects;
import org.minimalj.persistence.sql.SqlPersistence;

public class SqlCrudTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), A.class, G.class, H.class, M.class, N.class, O.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testInsertAndDelete() {
		G g = new G("testName1");
		Object id = persistence.insert(g);
		
		G g2 = persistence.read(G.class, id);
		Assert.assertNotNull(g2);
		
		persistence.delete(g2);
		
		G g3 = persistence.read(G.class, id);
		Assert.assertNull(g3);
	}
	
	@Test
	public void testInsertAndDeleteHistorized() {
		A a = new A("testName1");
		Object id = persistence.insert(a);
		
		A a2 = persistence.read(A.class, id);
		Assert.assertNotNull(a2);
		
		persistence.delete(a2);
		
		A a3 = persistence.read(A.class, id);
		Assert.assertNull(a3);
		
		List<Integer> version = persistence.readVersions(A.class, id);
		Assert.assertEquals("While delete, the old version should be still in the db", 1, version.size());
	}
	
	@Test
	public void testCrudWithSubtable() {
		A a = new A("testName1");
		
		a.b.add(new B("testNameB1"));
		a.b.add(new B("testNameB2"));
		
		a.c.add(new C("testNameC1"));
		a.c.add(new C("testNameC2"));
		a.c.add(new C("testNameC3"));

		Object id = persistence.insert(a);

		//
		
		A a2 = persistence.read(A.class, id);
		Assert.assertEquals("The string in the first B of A should match the original String", "testNameB1", a2.b.get(0).bName);
		Assert.assertEquals("The string in the second C of A should match the original String", "testNameC2", a2.c.get(1).cName);
		Assert.assertEquals("The count of the B's attached to A should match", 2, a2.b.size());
		Assert.assertEquals("The count of the C's attached to A should match", 3, a2.c.size());
	}
	
	@Test
	public void testSubtableVersion() {
		Object id = writeSimpleA();
		readTheAandAddBandE(id);
		
		List<Integer> versions = persistence.readVersions(A.class, id);
		Assert.assertEquals("A should now have 1 historized version", 1, versions.size());
		
		A a3 = persistence.readVersion(A.class, id, versions.get(0));
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
		Assert.assertEquals(1, persistence.readVersion(A.class, id, versions.get(3)).b.size());
		Assert.assertEquals(2, persistence.readVersion(A.class, id, versions.get(2)).b.size());
		Assert.assertEquals(1, persistence.readVersion(A.class, id, versions.get(1)).b.size());
		Assert.assertEquals(0, persistence.readVersion(A.class, id, versions.get(0)).b.size());
	}
	
	@Test
	public void testDependable() throws Exception {
		H h = new H();
		Object id = persistence.insert(h);

		h = persistence.read(H.class, id);
		h.k = new K("Test");
		persistence.update(h);

		h = persistence.read(H.class, id);
		Assert.assertNotNull("Dependable should be available", h.k);
		Assert.assertEquals("Content of dependable should be stored", "Test", h.k.k);
		
		h.k = null;
		persistence.update(h);

		h = persistence.read(H.class, id);
		Assert.assertNull("Dependable should be removed", h.k);
	}
	
	@Test(expected = RuntimeException.class)
	public void testDeleteIdentifiableChild() {
		N n = new N("testN");
		O o1 = new O("testO1");
		O o2 = new O("testO2");
		n.oList.add(o1);
		n.oList.add(o2);
		
		Object n_id = persistence.insert(n);
		n = persistence.read(N.class, n_id);
		
		Assert.assertEquals(2, n.oList.size());
		o1 = n.oList.get(0);
		o2 = n.oList.get(1);
		Assert.assertNotNull(o1.id);
		Assert.assertNotNull(o2.id);

		persistence.delete(o1);

		n = persistence.read(N.class, n_id);
		
		Assert.assertEquals(1, n.oList.size());
		o2 = n.oList.get(0);
		Assert.assertNotNull(o2.id);
	}

	@Test(expected = RuntimeException.class)
	public void testDeleteIdentifiableReference() {
		N n = new N("testN");
		O o = new O("testO");
		n.oReference = o;
		
		Object n_id = persistence.insert(n);
		n = persistence.read(N.class, n_id);
		
		Assert.assertNotNull(n.oReference);
		o = n.oReference;
		Assert.assertNotNull(o.id);

		persistence.delete(o);

		n = persistence.read(N.class, n_id);
		
		Assert.assertNull(n.oReference);
	}

	@Test
	public void testByteArray() throws Exception {
		M m = new M();
		Object id = persistence.insert(m);

		m = persistence.read(M.class, id);
		m.bytes = new byte[]{1,2,3};
		persistence.update(m);

		m = persistence.read(M.class, id);
		Assert.assertNotNull("Byte array should be available", m.bytes);
		Assert.assertEquals("Content of byte array should be stored", 3, m.bytes.length);
		
		persistence.delete(m);
	}

	private Object writeSimpleA() {
		A a = new A("testName1");

		Object id = persistence.insert(a);
		return id;
	}

	private void readTheAandAddBandE(Object id) {
		A a2 = persistence.read(A.class, id);
		a2.b.add(new B("testNameB1"));
		a2.e = new E();
		a2.e.e = "AddedE";
		
		persistence.update(a2);
	}

	private void addAnotherB(final A a4) {
		a4.b.add(new B("testNameB2"));
		persistence.update(a4);
	}

	private void removeFirstB(final Object id) {
		A a5 = persistence.read(A.class, id);
		a5.b.remove(0);
		persistence.update(a5);
	}

}
