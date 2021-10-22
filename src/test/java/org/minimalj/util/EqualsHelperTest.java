package org.minimalj.util;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class EqualsHelperTest {

	@Test public void 
	equalsPrimitives() {
		Assert.assertFalse("Not equals Class", EqualsHelper.equals(Integer.valueOf(42), "42"));

		Assert.assertTrue("Equals same Integers", EqualsHelper.equals(Integer.valueOf(42), Integer.valueOf(42)));
		Assert.assertFalse("Not equals Integers", EqualsHelper.equals(Integer.valueOf(42), Integer.valueOf(43)));

		Assert.assertTrue("Equals same Longs", EqualsHelper.equals(Long.valueOf(42), Long.valueOf(42)));
		Assert.assertFalse("Not equals Longs", EqualsHelper.equals(Long.valueOf(42), Long.valueOf(43)));

		Assert.assertTrue("Equals same Boolean", EqualsHelper.equals(true, Boolean.TRUE));
		Assert.assertFalse("Not equals Boolean", EqualsHelper.equals(false, Boolean.TRUE));
		Assert.assertFalse("Not equals Boolean", EqualsHelper.equals(null, Boolean.TRUE));

		Assert.assertTrue("Equals same String", EqualsHelper.equals("ab", "a" + "b"));
		Assert.assertTrue("Equals same String", EqualsHelper.equals("ab", new String("ab")));
		Assert.assertFalse("Not equals String", EqualsHelper.equals("ac", "ab"));

		Assert.assertTrue("Equals same EnumSet", EqualsHelper.equals(Set.of(EqualsHelperEnum.A), Set.of(EqualsHelperEnum.A)));
		Assert.assertFalse("Not equals EnumSet", EqualsHelper.equals(Set.of(EqualsHelperEnum.A), Set.of(EqualsHelperEnum.B)));
		Assert.assertFalse("Not equals EnumSet", EqualsHelper.equals(Set.of(EqualsHelperEnum.A), null));
		Assert.assertFalse("Not equals EnumSet", EqualsHelper.equals(Set.of(EqualsHelperEnum.A), new HashSet<>()));

		UUID id = UUID.randomUUID();
		Assert.assertTrue("Equals same UUID", EqualsHelper.equals(id, UUID.fromString(id.toString())));
		Assert.assertFalse("Not equals UUID", EqualsHelper.equals(UUID.randomUUID(), UUID.randomUUID()));
		
		Assert.assertTrue("Equals same BigDecimal value", EqualsHelper.equals(BigDecimal.valueOf(42), new BigDecimal(42)));
		Assert.assertTrue("Equals same BigDecimal with different scale", EqualsHelper.equals(BigDecimal.valueOf(42).setScale(2), new BigDecimal(42)));

		Assert.assertTrue("Equals same byte array", EqualsHelper.equals(new byte[] { 1 }, new byte[] { 1 }));
		Assert.assertFalse("Not equals byte array", EqualsHelper.equals(new byte[] { 1 }, new byte[] { 2 }));
		Assert.assertFalse("Not equals byte array", EqualsHelper.equals(new byte[] { 1 }, new byte[] { 1, 2 }));

		Assert.assertTrue("Equals same char array", EqualsHelper.equals(new char[] { 'a' }, new char[] { 'a' }));
		Assert.assertFalse("Not equals char array", EqualsHelper.equals(new char[] { 'a' }, new char[] { 'b' }));
		Assert.assertFalse("Not equals char array", EqualsHelper.equals(new char[] { 'a' }, new char[] { 'a', 'b' }));
	}

	@Test public void 
	equalsBigDecimal() {
		EqualsHelperTestA a1 = new EqualsHelperTestA();
		EqualsHelperTestA a2 = new EqualsHelperTestA();
		
		a1.a = "ab";
		a2.a = new String("ab");
		Assert.assertTrue("Equals same values", EqualsHelper.equals(a1, a2));

		a2.a = new String("ac");
		Assert.assertFalse("Not equals values", EqualsHelper.equals(a1, a2));
	}

	@Test public void 
	equalsField() {
		EqualsHelperTestA a1 = new EqualsHelperTestA();
		EqualsHelperTestA a2 = new EqualsHelperTestA();
		
		a1.a = "ab";
		a2.a = new String("ab");
		Assert.assertTrue("Equals same values", EqualsHelper.equals(a1, a2));

		a2.a = new String("ac");
		Assert.assertFalse("Not equals values", EqualsHelper.equals(a1, a2));
	}

	@Test public void 
	equalsTransientField() {
		EqualsHelperTestA a1 = new EqualsHelperTestA();
		EqualsHelperTestA a2 = new EqualsHelperTestA();
		
		a1.t = "ab";
		a2.t = new String("ab");
		Assert.assertTrue("Equals same values", EqualsHelper.equals(a1, a2));

		a2.t = new String("ac");
		Assert.assertFalse("Not equals values", EqualsHelper.equals(a1, a2));
	}

	@Test public void 
	equalsListField() {
		EqualsHelperTestA a1 = new EqualsHelperTestA();
		EqualsHelperTestA a2 = new EqualsHelperTestA();
		
		EqualsHelperTestB b1 = new EqualsHelperTestB();
		EqualsHelperTestB b2 = new EqualsHelperTestB();
		
		b1.b = "a";
		b2.b = "a";
		
		a1.c = List.of(b1);
		a2.c = List.of(b2);
		Assert.assertTrue("Equals same list", EqualsHelper.equals(a1, a2));

		a2.c = List.of(b1, b1);
		Assert.assertFalse("Not equals list", EqualsHelper.equals(a1, a2));

		a2.c = List.of(b1, b2);
		Assert.assertFalse("Not equals list", EqualsHelper.equals(a1, a2));
	}
	
	@Test public void 
	equalsId() {
		EqualsHelperTestC c1 = new EqualsHelperTestC();
		c1.id = 42;
		c1.a = "Text";
		
		EqualsHelperTestC c2 = new EqualsHelperTestC();
		c2.id = c1.id;
		c2.a = c1.a;
		
		Assert.assertTrue("Equals id equals values", EqualsHelper.equals(c1, c2));
		
		c2.a = "Different Text";
		Assert.assertFalse("Equals id but different values", EqualsHelper.equals(c1, c2));

		c2.id = 43;
		c2.a = c1.a;
		Assert.assertFalse("Not equals id (same values)", EqualsHelper.equals(c1, c2));
	}

	@Test public void 
	equalsArray() {
		EqualsHelperTestB[] bArray1 = new EqualsHelperTestB[1];
		EqualsHelperTestB b1 = new EqualsHelperTestB();
		b1.b = "text";
		bArray1[0] = b1;

		EqualsHelperTestB[] bArray2 = new EqualsHelperTestB[1];
		EqualsHelperTestB b2 = new EqualsHelperTestB();
		b2.b = "text";
		bArray2[0] = b2;

		Assert.assertTrue("Same values", EqualsHelper.equals(bArray1, bArray2));

		b2.b = "different text";
		Assert.assertFalse("Different values", EqualsHelper.equals(bArray1, bArray2));

		bArray2 = new EqualsHelperTestB[] { b2, b2 } ;
		Assert.assertFalse("Different length", EqualsHelper.equals(bArray1, bArray2));
	}
	
	public static class EqualsHelperTestA {
		public static String staticString = "staticString"; // just for test coverage
		
		public String a;
		public transient String t;
		public List<EqualsHelperTestB> c;
	}

	public static class  EqualsHelperTestB {
		public String b;
	}

	public static class  EqualsHelperTestC {
		public Object id;
		public String a;
	}

	public static enum EqualsHelperEnum {
		A, B;
	}

}
