package org.minimalj.model.properties;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.test.ModelTest;

public class FlatPropertiesTest {

	@Test public void 
	inline() {
		ModelTest modelTest = new ModelTest(TestEntityA1.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA.class).keySet().contains("field1"));
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA.class).keySet().contains("testEntityBInlineField1"));
	}

	@Test public void 
	inline_contains_same_field_as_containing() {
		ModelTest modelTest = new ModelTest(TestEntityA1.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA1.class).keySet().contains("field1"));
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA1.class).keySet().contains("testEntityBField1"));
	}

	@Test public void 
	two_inlines_contain_same_field() {
		ModelTest modelTest = new ModelTest(TestEntityA2.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA2.class).keySet().contains("field1"));
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA2.class).keySet().contains("testEntityB1Field1"));
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA2.class).keySet().contains("testEntityB2Field1"));
	}

	@Test public void 
	two_inlines_contain_same_field_one_with_class_name() {
		ModelTest modelTest = new ModelTest(TestEntityA3.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA3.class).keySet().contains("field1"));
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA3.class).keySet().contains("testEntityBField1"));
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA3.class).keySet().contains("testEntityB2Field1"));
	}

	@Test public void 
	inline_may_contain_field_with_its_name_in_container() {
		ModelTest modelTest = new ModelTest(TestEntityA4.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(TestEntityA4.class).keySet().contains("testEntityB2"));
	}
	
	@Test public void 
	final_Set_should_be_property() {
		ModelTest modelTest = new ModelTest(TestEntityC.class);
		Assert.assertTrue(modelTest.isValid());
		Assert.assertTrue(FlatProperties.getProperties(TestEntityC.class).keySet().contains("e"));
	}

	public static class TestEntityA {
		public Object id;
		public final TestEntityB testEntityBInline = new TestEntityB();
		public Integer field1;
	}

	public static class TestEntityA1 {
		public Object id;
		public final TestEntityB testEntityB = new TestEntityB();
		public Integer field1;
	}
	
	public static class TestEntityA2 {
		public Object id;
		public final TestEntityB testEntityB1 = new TestEntityB();
		public final TestEntityB testEntityB2 = new TestEntityB();
		public Integer field1;
	}
	
	public static class TestEntityA3 {
		public Object id;
		public final TestEntityB testEntityB = new TestEntityB();
		public final TestEntityB testEntityB2 = new TestEntityB();
		public Integer field1;
	}

	public static class TestEntityA4 {
		public Object id;
		public final TestEntityB2 testEntityB2 = new TestEntityB2();
	}

	public static class TestEntityB {
		public Integer field1;
	}
	
	public static class TestEntityB2 {
		public Integer testEntityB2;
	}
	
	public static enum TestEnum {
		A, B;
	}
	
	public static class TestEntityC {
		public Object id;
		public final Set<TestEnum> e = new TreeSet<>();
	}

}
