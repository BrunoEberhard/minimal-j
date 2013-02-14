package ch.openech.mj.db.model;

import junit.framework.Assert;

import org.junit.Test;

import ch.openech.test.db.A;
import ch.openech.test.db.B;

public class ColumnPropertiesTest {

	@Test
	public void testCopyAttribute() {
		A a = new A();
		A a2 = new A();
		a.aName = "test1";
		ColumnProperties.copy(a, a2);
		Assert.assertEquals(a.aName, a2.aName);
	}

	@Test
	public void testCopyList() {
		A a = new A();
		A a2 = new A();
		B b = new B();
		b.bName = "test2";
		ColumnProperties.copy(a, a2);
		Assert.assertEquals(a.b.size(), a2.b.size());
	}

	@Test
	public void testCopyToItself() {
		A a = new A();
		a.aName = "test1";
		B b = new B();
		b.bName = "test2";
		a.b.add(b);
		ColumnProperties.copy(a, a);
		Assert.assertEquals("test1", a.aName);
		Assert.assertEquals(1, a.b.size());
	}

}
