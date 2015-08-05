package org.minimalj.util;

import java.util.ArrayList;
import java.util.List;

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
	
	
	public static class CloneHelperTestA {
		public String a;
		public final List<CloneHelperTestB> b = new ArrayList<CloneHelperTestB>();
		public List<CloneHelperTestC> c = new ArrayList<CloneHelperTestC>();
		public List<CloneHelperTestC> empty;
		public transient String t;
		public final CloneHelperTestB b_inline = new CloneHelperTestB();
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

}
