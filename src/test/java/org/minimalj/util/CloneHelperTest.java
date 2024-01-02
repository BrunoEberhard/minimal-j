package org.minimalj.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CloneHelperTest {

	@Test public void 
	should_copy_string_attribute() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		a.a = "Hallo";
		CloneHelperTestA clone = CloneHelper.clone(a);
		Assert.assertEquals(a.a, clone.a);
	}
	
	@Test public void 
	should_copy_enum_attribute() throws Exception {
		CloneHelperTestE e = new CloneHelperTestE();
		e.f.add(CloneHelperTestF.B);
		CloneHelperTestE clone = CloneHelper.clone(e);
		Assert.assertTrue(clone.f.size() == 1);
		Assert.assertTrue(clone.f.iterator().next() == CloneHelperTestF.B);
	}
	
	@Test public void 
	should_copy_transient_attribute() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		a.t = "Transient";
		CloneHelperTestA clone = CloneHelper.clone(a);
		Assert.assertEquals(a.t, clone.t);
	}
	
	@Test public void 
	should_copy_inline_attribute() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		a.b_inline.b = "b";
		CloneHelperTestA clone = CloneHelper.clone(a);
		Assert.assertEquals(a.b_inline.b, clone.b_inline.b);
	}
	
	@Test public void 
	should_copy_list() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		CloneHelperTestB b = new CloneHelperTestB();
		b.b = "Du";
		a.b.add(b);
		
		CloneHelperTestA clone = CloneHelper.clone(a);
		
		Assert.assertEquals(a.b.size(), clone.b.size());
	}

	@Test public void 
	should_generate_new_instances_for_list_items() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		CloneHelperTestB b = new CloneHelperTestB();
		b.b = "Du";
		a.b.add(b);
		
		CloneHelperTestA clone = CloneHelper.clone(a);
		
		Assert.assertNotSame(a.b.get(0), clone.b.get(0));
	}

	@Test public void 
	should_copy_attributes_of_list_items() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		CloneHelperTestB b = new CloneHelperTestB();
		b.b = "Du";
		a.b.add(b);
		
		CloneHelperTestA clone = CloneHelper.clone(a);
		
		Assert.assertEquals(a.b.get(0).b, clone.b.get(0).b);
	}
	
	@Test public void 
	should_copy_list_with_lists() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		CloneHelperTestC c = new CloneHelperTestC();
		c.c = "Test";
		c.d = new CloneHelperTestD();
		c.d.d = 23;
		a.c = new ArrayList<>();
		a.c.add(c);
		
		CloneHelperTestA clone = CloneHelper.clone(a);
		
		Assert.assertEquals(a.c.get(0).d.d, clone.c.get(0).d.d);
	}
	
	@Test public void 
	should_leave_alone_null_lists() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		CloneHelperTestA clone = CloneHelper.clone(a);
		
		Assert.assertEquals(null, clone.empty);
	}

	@Test public void 
	should_copy_bytes() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		a.bytes = new byte[] { 1, 2	};
		CloneHelperTestA clone = CloneHelper.clone(a);

		Assert.assertEquals(2, clone.bytes.length);
	}

	@Test public void 
	should_copy_chars() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		a.chars = new char[] { 'a' };
		CloneHelperTestA clone = CloneHelper.clone(a);

		Assert.assertEquals(1, clone.chars.length);
	}

	@Test public void 
	should_copy_array() throws Exception {
		CloneHelperTestA a = new CloneHelperTestA();
		CloneHelperTestB b = new CloneHelperTestB();
		a.genericArray = new CloneHelperTestB[] { b };
		CloneHelperTestA clone = CloneHelper.clone(a);

		Assert.assertEquals(1, clone.genericArray.length);
	}

	public static class CloneHelperTestA {
		public String a;
		public final List<CloneHelperTestB> b = new ArrayList<>();
		public List<CloneHelperTestC> c;
		public List<CloneHelperTestC> empty;
		public transient String t;
		public final CloneHelperTestB b_inline = new CloneHelperTestB();
		public byte[] bytes;
		public char[] chars;
		public CloneHelperTestB[] genericArray;
	}

	public static class  CloneHelperTestB {
		public String b;
	}

	public static class  CloneHelperTestC {
		public String c;
		public CloneHelperTestD d;
	}
	
	public static class  CloneHelperTestD {
		public Integer d;
	}

	public static class CloneHelperTestE {
		public final Set<CloneHelperTestF> f = new HashSet<>();
	}
	
	public static enum CloneHelperTestF {
		A, B, C;
	}
	
}
