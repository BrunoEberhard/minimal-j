package org.minimalj.model.properties;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.test.ModelTest;

public class FlatPropertiesTest {

	@Test public void 
	inline() {
		ModelTest modelTest = new ModelTest(A1.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(A.class).keySet().contains("field1"));
		Assert.assertTrue(FlatProperties.getProperties(A.class).keySet().contains("bInlineField1"));
	}

	@Test public void 
	inline_contains_same_field_as_containing() {
		ModelTest modelTest = new ModelTest(A1.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(A1.class).keySet().contains("field1"));
		Assert.assertTrue(FlatProperties.getProperties(A1.class).keySet().contains("bField1"));
	}

	@Test public void 
	two_inlines_contain_same_field() {
		ModelTest modelTest = new ModelTest(A2.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(A2.class).keySet().contains("field1"));
		Assert.assertTrue(FlatProperties.getProperties(A2.class).keySet().contains("b1Field1"));
		Assert.assertTrue(FlatProperties.getProperties(A2.class).keySet().contains("b2Field1"));
	}

	@Test public void 
	two_inlines_contain_same_field_one_with_class_name() {
		ModelTest modelTest = new ModelTest(A3.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(A3.class).keySet().contains("field1"));
		Assert.assertTrue(FlatProperties.getProperties(A3.class).keySet().contains("bField1"));
		Assert.assertTrue(FlatProperties.getProperties(A3.class).keySet().contains("b2Field1"));
	}

	@Test public void 
	inline_may_contain_field_with_its_name_in_container() {
		ModelTest modelTest = new ModelTest(A4.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(A4.class).keySet().contains("b2"));
	}

	public static class A {
		public Object id;
		public final B bInline = new B();
		public Integer field1;
	}

	public static class A1 {
		public Object id;
		public final B b = new B();
		public Integer field1;
	}
	
	public static class A2 {
		public Object id;
		public final B b1 = new B();
		public final B b2 = new B();
		public Integer field1;
	}
	
	public static class A3 {
		public Object id;
		public final B b = new B();
		public final B b2 = new B();
		public Integer field1;
	}

	public static class A4 {
		public Object id;
		public final B2 b2 = new B2();
	}

	public static class B {
		public Integer field1;
	}
	
	public static class B2 {
		public Integer b2;
	}

}
