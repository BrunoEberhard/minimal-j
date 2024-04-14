package org.minimalj.repository.sql;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.util.IdUtils;

public class SqlHistorizedCrudTest {
	
	private static SqlHistorizedRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlHistorizedRepository(DataSourceFactory.embeddedDataSource(), A.class, F.class);
	}
	
	@AfterClass
	public static void shutdownRepository() {
	}
	
	@Test
	public void testInsertAndDeleteHistorized() {
		A a = new A("testName1");
		Object id = repository.insert(a);
		
		A a2 = repository.read(A.class, id);
		Assert.assertNotNull(a2);
		
		repository.delete(a2);
		
		A a3 = repository.read(A.class, id);
		Assert.assertNull(a3);
		
		List<A> history = repository.loadHistory(A.class, id, 1);
		Assert.assertFalse(history.isEmpty());

		Assert.assertTrue(history.get(0).historized);
	}
	
	@Test
	public void testSubtableVersion() {
		Object id = writeSimpleA();
		readTheAandAddBandE(id);
		
		A a = repository.read(A.class, id);
		int version = IdUtils.getVersion(a);
		Assert.assertEquals("A should now have 2 versions (0 and 1)", 1, version);
		
		A a3 = repository.readVersion(A.class, id, 0);
		Assert.assertEquals("The historized (first) version of A should not have any B attached", 0, a3.b.size());
		Assert.assertTrue("The historized (first) version of A should not have a E attached", EmptyObjects.isEmpty(a3.e));
		
		A a4 = repository.readVersion(A.class, id, 1);
		Assert.assertEquals("The actual version of A should have a B attached", 1, a4.b.size());
		Assert.assertNotNull("The actual version of A should have a E attached", a4.e);

		addAnotherB(a4);
		removeFirstB(id);
		removeFirstB(id);
		
		a = repository.read(A.class, id);
		version = IdUtils.getVersion(a);
		Assert.assertEquals("A should now have 4 historized versions and the actual should be version 4", 4, version);

		Assert.assertEquals("Every B should be removed from the A now", 0, repository.read(A.class, id).b.size());
		
		// now check for the right amount of B's attached to A in every version
		Assert.assertEquals(1, repository.readVersion(A.class, id, 3).b.size());
		Assert.assertEquals(2, repository.readVersion(A.class, id, 2).b.size());
		Assert.assertEquals(1, repository.readVersion(A.class, id, 1).b.size());
		Assert.assertEquals(0, repository.readVersion(A.class, id, 0).b.size());
	}
	
	@Test
	public void testHistorizedReference() {
		Object id = writeSimpleA();
		A a = repository.read(A.class, id);

		a.f = new F();
		a.f.f = "F";
		repository.update(a);
		
		A a2 = repository.read(A.class, id);
		F f = a2.f;
		f.f = "newF";
		repository.update(f);
		
		A a3 = repository.read(A.class, id);
		Assert.assertEquals("Even when a new version of referenced Object was created still the old one should be referenced", "F", a3.f.f);
	}

	@Test
	public void testHistorizedExistingReference() {
		Object id = writeSimpleA();
		A a = repository.read(A.class, id);

		// create an referenced entity and change it once
		// (so that version is not 0 anymore)
		F f = new F();
		f.f = "F";
		Object fId = repository.insert(f);
		f = repository.read(F.class, fId);
		f.f = "F2";
		repository.update(f);
		f = repository.read(F.class, fId);

		// use second version in a
		a.f = f;
		repository.update(a);

		// update f to a third version
		f.f = "F3";
		repository.update(f);

		// read a again (should point to 2nd version, not third)
		A a3 = repository.read(A.class, id);
		Assert.assertEquals("Even when a new version of referenced Object was created still the old one should be referenced", "F2", a3.f.f);
	}

	private Object writeSimpleA() {
		A a = new A("testName1");

		Object id = repository.insert(a);
		return id;
	}

	private void readTheAandAddBandE(Object id) {
		A a2 = repository.read(A.class, id);
		a2.b.add(new B("testNameB1"));
		a2.e = new E();
		a2.e.e = "AddedE";
		
		repository.update(a2);
	}

	private void addAnotherB(final A a4) {
		a4.b.add(new B("testNameB2"));
		repository.update(a4);
	}

	private void removeFirstB(final Object id) {
		A a5 = repository.read(A.class, id);
		a5.b.remove(0);
		repository.update(a5);
	}
	
}