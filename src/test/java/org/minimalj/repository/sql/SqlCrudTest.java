package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.EqualsHelper;

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
	public void testInsertShouldNotChangeInput() {
		G g = new G("testName1");
		G g_clone = CloneHelper.clone(g);
		repository.insert(g);
		Assert.assertTrue("Insert should not update the id Field", EqualsHelper.equals(g, g_clone));
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

		TestElement elementToDelete = e1;
		executeWithoutLog(() -> repository.delete(elementToDelete));
	}
	
	public static void executeWithoutLog(Runnable r) {
		Level logLevel = AbstractTable.sqlLogger.getLevel();
		try {
			AbstractTable.sqlLogger.setLevel(Level.OFF);
			r.run();
		} finally {
			AbstractTable.sqlLogger.setLevel(logLevel);
		}
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

		TestElement elementToDelete = e;
		executeWithoutLog(() -> repository.delete(elementToDelete)); 
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
		
		public List<TestElement> testElementList = new ArrayList<>();

		public TestElement testElementReference;
	}

}
