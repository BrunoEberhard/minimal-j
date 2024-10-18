package org.minimalj.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ModelTest {

	@Test
	public void testDepthFirst() {
		List<Class<?>> classes = Model.getClassesRecursive(new Class[] {TestClass1.class, TestClass5.class}, true, true);
		Assert.assertEquals(4, classes.size());
		Assert.assertTrue(classes.indexOf(TestClass3.class) < classes.indexOf(TestClass2.class));
		Assert.assertTrue(classes.indexOf(TestClass2.class) < classes.indexOf(TestClass1.class));
		Assert.assertTrue(classes.indexOf(TestClass2.class) < classes.indexOf(TestClass5.class));
	}
	
	public static class TestClass1 {
		public Object id;
		public TestClass2 testClass2;
	}
	
	public static class TestClass2 {
		public Object id;
		public TestClass3 testClass3;
		public TestClass4 testClass4;
	}

	public static class TestClass3 {
		public Object id;
	}

	public static class TestClass4 {
		
	}
	
	public static class TestClass5 {
		public Object id;
		public TestClass2 testClass2;
	}
}