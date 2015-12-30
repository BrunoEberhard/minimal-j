package org.minimalj.model.properties;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;

public class ChainedPropertyTest {

	@Test public void 
	available() {
		C c = new C();
		c.text = "Hello";
		B b = new B();
		b.c = c;
		A a = new A();
		a.b = b;
		Assert.assertTrue(((ChainedProperty) Keys.getProperty(A.$.b.c.text)).isAvailableFor(a));
		// value of field should not matter
		c.text = null;
		Assert.assertTrue(((ChainedProperty) Keys.getProperty(A.$.b.c.text)).isAvailableFor(a));
	}
	
	@Test public void 
	not_available1() {
		B b = new B();
		A a = new A();
		a.b = b;
		Assert.assertFalse(((ChainedProperty) Keys.getProperty(A.$.b.c.text)).isAvailableFor(a));
	}
	
	@Test public void 
	not_available() {
		A a = new A();
		Assert.assertFalse(((ChainedProperty) Keys.getProperty(A.$.b.c.text)).isAvailableFor(a));
	}
	
	public static class A {
		public static final A $ = Keys.of(A.class);
		
		public B b;
	}
	
	public static class B {
		public C c;
	}
	
	public static class C {
		public String text;
	}
	
}
