package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.util.IdUtils;

public class SqlCrudTest {
	
	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), A.class, G.class, H.class, M.class, TestEntity.class, TestElement.class);
	}
	
	@AfterClass
	public static void shutdownRepository() {
	}
	
	@Test
	public void testInsertAndDelete() {
		G g = new G("testName1");
		Object id = repository.insert(g);
		
		G g2 = repository.read(G.class, id);
		Assert.assertNotNull(g2);
		
		repository.delete(g2);
		
		G g3 = repository.read(G.class, id);
		Assert.assertNull(g3);
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
	public void testCrudWithSubtable() {
		A a = new A("testName1");
		
		a.b.add(new B("testNameB1"));
		a.b.add(new B("testNameB2"));
		
		a.c.add(new C("testNameC1"));
		a.c.add(new C("testNameC2"));
		a.c.add(new C("testNameC3"));

		Object id = repository.insert(a);

		//
		
		A a2 = repository.read(A.class, id);
		Assert.assertEquals("The string in the first B of A should match the original String", "testNameB1", a2.b.get(0).bName);
		Assert.assertEquals("The string in the second C of A should match the original String", "testNameC2", a2.c.get(1).cName);
		Assert.assertEquals("The count of the B's attached to A should match", 2, a2.b.size());
		Assert.assertEquals("The count of the C's attached to A should match", 3, a2.c.size());
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
	public void testDependable() throws Exception {
		H h = new H();
		Object id = repository.insert(h);

		h = repository.read(H.class, id);
		h.k = new K("Test");
		repository.update(h);

		h = repository.read(H.class, id);
		Assert.assertNotNull("Dependable should be available", h.k);
		Assert.assertEquals("Content of dependable should be stored", "Test", h.k.k);
		
		h.k = null;
		repository.update(h);

		h = repository.read(H.class, id);
		Assert.assertNull("Dependable should be removed", h.k);
	}
	
	@Test(expected = RuntimeException.class)
	public void testDeleteIdentifiableChild() {
		TestEntity entity = new TestEntity("testN");
		TestElement e1 = new TestElement("testO1");
		TestElement e2 = new TestElement("testO2");
		entity.testElementList.add(e1);
		entity.testElementList.add(e2);
		
		Object entity_id = repository.insert(entity);
		entity = repository.read(TestEntity.class, entity_id);
		
		Assert.assertEquals(2, entity.testElementList.size());
		e1 = entity.testElementList.get(0);
		e2 = entity.testElementList.get(1);
		Assert.assertNotNull(e1.id);
		Assert.assertNotNull(e2.id);

		repository.delete(e1);

		entity = repository.read(TestEntity.class, entity_id);
		
		Assert.assertEquals(1, entity.testElementList.size());
		e2 = entity.testElementList.get(0);
		Assert.assertNotNull(e2.id);
	}

	@Test(expected = RuntimeException.class)
	public void testDeleteIdentifiableReference() {
		TestEntity entity = new TestEntity("testN");
		TestElement e = new TestElement("testO");
		entity.testElementReference = e;
		
		Object element_id = repository.insert(entity);
		entity = repository.read(TestEntity.class, element_id);
		
		Assert.assertNotNull(entity.testElementReference);
		e = entity.testElementReference;
		Assert.assertNotNull(e.id);

		repository.delete(e);

		entity = repository.read(TestEntity.class, element_id);
		
		Assert.assertNull(entity.testElementReference);
	}

	@Test
	public void testByteArray() throws Exception {
		M m = new M();
		Object id = repository.insert(m);

		m = repository.read(M.class, id);
		m.bytes = new byte[]{1,2,3};
		repository.update(m);

		m = repository.read(M.class, id);
		Assert.assertNotNull("Byte array should be available", m.bytes);
		Assert.assertEquals("Content of byte array should be stored", 3, m.bytes.length);
		
		repository.delete(m);
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

	//
	
	public static class TestElement {
		public static final TestElement $ = Keys.of(TestElement.class);
		
		public TestElement() {
			// needed for reflection constructor
		}
		
		public TestElement(String testElementName) {
			this.testElementName = testElementName;
		}
		
		public Object id;
		
		@Size(30)
		public String testElementName;
		
	}

	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public TestEntity() {
			// needed for reflection constructor
		}

		public TestEntity(String testElement) {
			this.testElement = testElement;
		}

		public Object id;

		@Size(20)
		public String testElement;
		
		public final List<TestElement> testElementList = new ArrayList<>();

		public TestElement testElementReference;
	}

}
