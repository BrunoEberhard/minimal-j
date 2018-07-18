package org.minimalj.model.properties;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PropertiesTest {

	@Test
	public void testPopulate() {
		TestClassA a = new TestClassA();
		PropertyInterface p = Properties.getPropertyByPath(TestClassA.class, "b.c.d.valueD");
		
		Properties.setAndRestructure(p, a, Integer.valueOf(123));
		
		Assert.assertNotNull(a.b);
		Assert.assertNotNull(a.b.c);
		Assert.assertNotNull(a.b.c.d);
		Assert.assertEquals(Integer.valueOf(123), a.b.c.d.valueD);
	}
	
	@Test
	public void testPopulateWithNull() {
		TestClassA a = new TestClassA();
		PropertyInterface p = Properties.getPropertyByPath(TestClassA.class, "b.c.d.valueD");
		
		Properties.setAndRestructure(p, a, null);

		Assert.assertNull(a.b);
	}
	
	@Test
	public void testDepopulate() {
		TestClassA a = new TestClassA();
		PropertyInterface p = Properties.getPropertyByPath(TestClassA.class, "b.c.d.valueD");
		
		Properties.setAndRestructure(p, a, Integer.valueOf(123));
		Properties.setAndRestructure(p, a, null);
		
		Assert.assertNull(a.b);
	}
	
	@Test
	public void testDepopulateFinal() {
		TestClassA a = new TestClassA();
		PropertyInterface p = Properties.getPropertyByPath(TestClassA.class, "b_final.c.d.valueD");
		
		Properties.setAndRestructure(p, a, Integer.valueOf(123));
		Properties.setAndRestructure(p, a, null);
		
		Assert.assertNull(a.b_final.c);
	}
	
	public static class TestClassA {
		public TestClassB b;
		public TestClassB b_alreadyInitialized = new TestClassB();
		public final TestClassB b_final = new TestClassB();
	}

	public static class TestClassB {
		public TestClassC c;
		public Integer valueB;
	}

	public static class TestClassC {
		public TestClassD d;
		public List<TestClassD> dList;
	}

	public static class TestClassD {
		public Integer valueD;
	}
}
