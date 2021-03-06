package org.minimalj.model.properties;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;

public class ChainedPropertyTest {

	@Test public void 
	available() {
		TestEntityA a = testData();
		Assert.assertTrue(((ChainedProperty) Keys.getProperty(TestEntityA.$.b.c.text)).isAvailableFor(a));
		// value of field should not matter
		a.b.c.text = null;
		Assert.assertTrue(((ChainedProperty) Keys.getProperty(TestEntityA.$.b.c.text)).isAvailableFor(a));
	}
	
	@Test public void 
	not_available1() {
		TestEntityB b = new TestEntityB();
		TestEntityA a = new TestEntityA();
		a.b = b;
		Assert.assertFalse(((ChainedProperty) Keys.getProperty(TestEntityA.$.b.c.text)).isAvailableFor(a));
	}
	
	@Test public void 
	not_available() {
		TestEntityA a = new TestEntityA();
		Assert.assertFalse(((ChainedProperty) Keys.getProperty(TestEntityA.$.b.c.text)).isAvailableFor(a));
	}
	
	@Test public void getByname() {
		TestEntityA a = testData();
		PropertyInterface property = Properties.getPropertyByPath(TestEntityA.class, "b.c.text");
		Assert.assertNotNull(property);
		Assert.assertEquals("Hello", property.getValue(a));
	}

	//
	
	private TestEntityA testData() {
		TestEntityC c = new TestEntityC();
		c.text = "Hello";
		TestEntityB b = new TestEntityB();
		b.c = c;
		TestEntityA a = new TestEntityA();
		a.b = b;
		return a;
	}
	
	public static class TestEntityA {
		public static final TestEntityA $ = Keys.of(TestEntityA.class);
		
		public TestEntityB b;
	}
	
	public static class TestEntityB {
		public TestEntityC c;
	}
	
	public static class TestEntityC {
		public String text;
	}
	
}
